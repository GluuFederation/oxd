package org.gluu.oxd.server.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.gluu.oxauth.client.*;
import org.gluu.oxauth.model.common.AuthenticationMethod;
import org.gluu.oxauth.model.common.GrantType;
import org.gluu.oxauth.model.common.Prompt;
import org.gluu.oxauth.model.common.ResponseType;
import org.gluu.oxauth.model.uma.UmaScopeType;
import org.gluu.oxauth.model.util.Util;
import org.gluu.oxd.common.CoreUtils;
import org.gluu.oxd.common.ErrorResponseCode;
import org.gluu.oxd.server.HttpException;
import org.gluu.oxd.server.OxdServerConfiguration;
import org.gluu.oxd.server.model.Pat;
import org.gluu.oxd.server.model.Token;
import org.gluu.oxd.server.model.TokenFactory;
import org.gluu.oxd.server.model.UmaTokenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TokenService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);

    private final RpService rpService;
    private final ValidationService validationService;
    private final DiscoveryService discoveryService;
    private final HttpService httpService;
    private final OxdServerConfiguration configuration;
    private final StateService stateService;

    @Inject
    public TokenService(RpService rpService,
                           ValidationService validationService,
                           DiscoveryService discoveryService,
                           HttpService httpService,
                           OxdServerConfiguration configuration,
                           StateService stateService
    ) {
        this.rpService = rpService;
        this.validationService = validationService;
        this.discoveryService = discoveryService;
        this.httpService = httpService;
        this.configuration = configuration;
        this.stateService = stateService;
    }

    private Pat obtainPat(String oxdId) {
        Rp site = rpService.getRp(oxdId);
        Token token = obtainToken(oxdId, site);

        site.setPat(token.getToken());
        site.setPatCreatedAt(new Date());
        site.setPatExpiresIn(token.getExpiresIn());
        site.setPatRefreshToken(token.getRefreshToken());

        rpService.updateSilently(site);

        return (Pat) token;
    }

    public Pat getToken(String oxdId) {
        validationService.notBlankOxdId(oxdId);

        Rp site = rpService.getRp(oxdId);

        if (site.getPat() != null && site.getPatCreatedAt() != null && site.getPatExpiresIn() > 0) {
            Calendar expiredAt = Calendar.getInstance();
            expiredAt.setTime(site.getPatCreatedAt());
            expiredAt.add(Calendar.SECOND, site.getPatExpiresIn());

            if (!CoreUtils.isExpired(expiredAt.getTime())) {
                LOG.debug("PAT from site configuration, PAT: " + site.getPat());
                return new Pat(site.getPat(), "", site.getPatExpiresIn());
            }
        }

        return obtainPat(oxdId);
    }

    public boolean useClientAuthentication() {
        return configuration.getUseClientAuthenticationForPat() != null && configuration.getUseClientAuthenticationForPat();
    }

    private Token obtainToken(String oxdId, Rp site) {

        OpenIdConfigurationResponse discovery = discoveryService.getConnectDiscoveryResponseByOxdId(oxdId);

        final Token token;
        if (useClientAuthentication()) {
            token = obtainTokenWithClientCredentials(discovery, site);
            LOG.trace("Obtained token with client authentication: " + token);
        } else {
            token = obtainTokenWithUserCredentials(discovery, site);
            LOG.trace("Obtained token with user credentials: " + token);
        }

        return token;
    }

    private Token obtainTokenWithClientCredentials(OpenIdConfigurationResponse discovery, Rp site) {
        final TokenClient tokenClient = new TokenClient(discovery.getTokenEndpoint());
        tokenClient.setExecutor(httpService.getClientExecutor());
        final TokenResponse response = tokenClient.execClientCredentialsGrant("openid", site.getClientId(), site.getClientSecret());
        if (response != null) {
            if (Util.allNotBlank(response.getAccessToken())) {

                final Token opResponse = TokenFactory.newToken();
                opResponse.setToken(response.getAccessToken());
                opResponse.setRefreshToken(response.getRefreshToken());
                opResponse.setExpiresIn(response.getExpiresIn());
                return opResponse;
            } else {
                LOG.error("Token is blank in response, site: " + site);
            }
        } else {
            LOG.error("No response from TokenClient");
        }
        throw new RuntimeException("Failed to obtain PAT.");
    }

    private Token obtainTokenWithUserCredentials(OpenIdConfigurationResponse discovery, Rp site) {

        // 1. Request authorization and receive the authorization code.
        final List<ResponseType> responseTypes = Lists.newArrayList();
        responseTypes.add(ResponseType.CODE);
        responseTypes.add(ResponseType.ID_TOKEN);

        final String state = stateService.generateState();

        final AuthorizationRequest request = new AuthorizationRequest(responseTypes, site.getClientId(), Lists.newArrayList("openid"), site.getRedirectUri(), null);
        request.setState(state);
        request.setAuthUsername(site.getUserId());
        request.setAuthPassword(site.getUserSecret());
        request.getPrompts().add(Prompt.NONE);

        final AuthorizeClient authorizeClient = new AuthorizeClient(discovery.getAuthorizationEndpoint());
        authorizeClient.setExecutor(httpService.getClientExecutor());
        authorizeClient.setRequest(request);
        final AuthorizationResponse response1 = authorizeClient.exec();

        ClientUtils.showClient(authorizeClient);

        final String scope = response1.getScope();
        final String authorizationCode = response1.getCode();
        if (!state.equals(response1.getState())) {
            throw new HttpException(ErrorResponseCode.INVALID_STATE);
        }

        if (Util.allNotBlank(authorizationCode)) {

            // 2. Request access token using the authorization code.
            final TokenRequest tokenRequest = new TokenRequest(GrantType.AUTHORIZATION_CODE);
            tokenRequest.setCode(authorizationCode);
            tokenRequest.setRedirectUri(site.getRedirectUri());
            tokenRequest.setAuthUsername(site.getClientId());
            tokenRequest.setAuthPassword(site.getClientSecret());
            tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);
            tokenRequest.setScope(scope);

            final TokenClient tokenClient1 = new TokenClient(discovery.getTokenEndpoint());
            tokenClient1.setRequest(tokenRequest);
            tokenClient1.setExecutor(httpService.getClientExecutor());
            final TokenResponse response2 = tokenClient1.exec();
            ClientUtils.showClient(authorizeClient);

            if (response2.getStatus() == 200 && Util.allNotBlank(response2.getAccessToken())) {
                final Token token = TokenFactory.newToken();
                token.setToken(response2.getAccessToken());
                token.setRefreshToken(response2.getRefreshToken());
                token.setExpiresIn(response2.getExpiresIn());
                return token;
            } else {
                LOG.error("Status: " + response2.getStatus() + ", Entity: " + response2.getEntity());
            }
        } else {
            LOG.debug("Authorization code is blank.");
        }
        throw new RuntimeException("Failed to obtain Token, scopeType: openid, site: " + site);
    }

}
