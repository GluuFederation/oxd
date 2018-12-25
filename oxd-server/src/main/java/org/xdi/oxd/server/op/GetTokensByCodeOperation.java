package org.xdi.oxd.server.op;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.oxauth.client.ClientUtils;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenRequest;
import org.xdi.oxauth.client.TokenResponse;
import org.xdi.oxauth.model.common.AuthenticationMethod;
import org.xdi.oxauth.model.common.GrantType;
import org.xdi.oxauth.model.exception.InvalidJwtException;
import org.xdi.oxauth.model.jwt.Jwt;
import org.xdi.oxauth.model.token.TokenErrorResponseType;
import org.xdi.oxd.common.Command;
import org.xdi.oxd.common.ErrorResponseCode;
import org.xdi.oxd.common.params.GetTokensByCodeParams;
import org.xdi.oxd.common.response.GetTokensByCodeResponse;
import org.xdi.oxd.common.response.IOpResponse;
import org.xdi.oxd.server.HttpException;
import org.xdi.oxd.server.service.Rp;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.xdi.oxauth.model.token.TokenErrorResponseType.INVALID_GRANT;
import static org.xdi.oxd.common.ErrorResponseCode.INTERNAL_ERROR_NO_PARAMS;
import static org.xdi.oxd.common.ErrorResponseCode.INVALID_AUTHORIZATION_CODE_BAD_CODE;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 22/09/2015
 */

public class GetTokensByCodeOperation extends BaseOperation<GetTokensByCodeParams> {

    private static final Logger LOG = LoggerFactory.getLogger(GetTokensByCodeOperation.class);

    /**
     * Base constructor
     *
     * @param command command
     */
    protected GetTokensByCodeOperation(Command command, final Injector injector) {
        super(command, injector, GetTokensByCodeParams.class);
    }

    @Override
    public IOpResponse execute(GetTokensByCodeParams params) throws Exception {
        validate(params);

        final Rp site = getRp();
        OpenIdConfigurationResponse discoveryResponse = getDiscoveryService().getConnectDiscoveryResponse(site);

        final TokenRequest tokenRequest = new TokenRequest(GrantType.AUTHORIZATION_CODE);
        tokenRequest.setCode(params.getCode());
        tokenRequest.setRedirectUri(site.getAuthorizationRedirectUri());
        tokenRequest.setAuthUsername(site.getClientId());
        tokenRequest.setAuthPassword(site.getClientSecret());
        tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);


        final TokenClient tokenClient = new TokenClient(discoveryResponse.getTokenEndpoint());
        tokenClient.setExecutor(getHttpService().getClientExecutor());
        tokenClient.setRequest(tokenRequest);
        final TokenResponse response = tokenClient.exec();
        ClientUtils.showClient(tokenClient);

        return validateResponse(params, site, discoveryResponse, response);
    }

    public IOpResponse validateResponse(GetTokensByCodeParams params, Rp site, OpenIdConfigurationResponse discoveryResponse, TokenResponse response) throws InvalidJwtException, IOException {
        if (response.getStatus() == 200 || response.getStatus() == 302) { // success or redirect

            return handleSuccess(params, site, discoveryResponse, response);

        } else if (response.getStatus() == 400) {

            return handleBadRequest(response);

        }

        LOG.error("Failed to get tokens because response code is: " + response.getScope());

        return null;
    }

    public IOpResponse handleSuccess(GetTokensByCodeParams params, Rp site, OpenIdConfigurationResponse discoveryResponse, TokenResponse response) throws InvalidJwtException, IOException {
        if (Strings.isNullOrEmpty(response.getIdToken())) {
            LOG.error("id_token is not returned. Please check whether 'openid' scope is present for 'get_authorization_url' command");
            throw new HttpException(ErrorResponseCode.NO_ID_TOKEN_RETURNED);
        }

        if (Strings.isNullOrEmpty(response.getAccessToken())) {
            LOG.error("access_token is not returned");
            throw new HttpException(ErrorResponseCode.NO_ACCESS_TOKEN_RETURNED);
        }

        final Jwt idToken = Jwt.parse(response.getIdToken());

        final Validator validator = new Validator(idToken, discoveryResponse, getKeyService());
        validator.validateNonce(getStateService());
        validator.validateIdToken(site.getClientId());
        validator.validateAccessToken(response.getAccessToken());

        // persist tokens
        site.setIdToken(response.getIdToken());
        site.setAccessToken(response.getAccessToken());
        getRpService().update(site);
        getStateService().invalidateState(params.getState());

        LOG.trace("Scope: " + response.getScope());

        final Map<String, List<String>> claims = idToken.getClaims() != null ? idToken.getClaims().toMap() : new HashMap<String, List<String>>();

        final GetTokensByCodeResponse opResponse = new GetTokensByCodeResponse();
        opResponse.setAccessToken(response.getAccessToken());
        opResponse.setIdToken(response.getIdToken());
        opResponse.setRefreshToken(response.getRefreshToken());
        opResponse.setExpiresIn(response.getExpiresIn() != null ? response.getExpiresIn() : -1);
        opResponse.setIdTokenClaims(claims);
        return opResponse;
    }

    public IOpResponse handleBadRequest(TokenResponse response) {
        final TokenErrorResponseType errorType = response.getErrorType();
        final ErrorResponseCode responseCode;

        if (errorType == INVALID_GRANT) {
            responseCode = INVALID_AUTHORIZATION_CODE_BAD_CODE;
        } else {
            responseCode = INTERNAL_ERROR_NO_PARAMS;
        }

        LOG.error(response.getErrorDescription());
        throw new HttpException(responseCode);
    }

    private void validate(GetTokensByCodeParams params) {
        if (Strings.isNullOrEmpty(params.getCode())) {
            throw new HttpException(ErrorResponseCode.BAD_REQUEST_NO_CODE);
        }
        if (Strings.isNullOrEmpty(params.getState())) {
            throw new HttpException(ErrorResponseCode.BAD_REQUEST_NO_STATE);
        }
        if (!getStateService().isStateValid(params.getState())) {
            throw new HttpException(ErrorResponseCode.BAD_REQUEST_STATE_NOT_VALID);
        }
    }
}
