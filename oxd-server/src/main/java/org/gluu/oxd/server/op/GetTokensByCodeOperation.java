package org.gluu.oxd.server.op;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import org.gluu.oxauth.client.OpenIdConfigurationResponse;
import org.gluu.oxauth.client.TokenClient;
import org.gluu.oxauth.client.TokenRequest;
import org.gluu.oxauth.client.TokenResponse;
import org.gluu.oxauth.model.common.AuthenticationMethod;
import org.gluu.oxauth.model.common.GrantType;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.gluu.oxauth.model.jwk.Algorithm;
import org.gluu.oxauth.model.jwk.Use;
import org.gluu.oxauth.model.jwt.Jwt;
import org.gluu.oxd.common.Command;
import org.gluu.oxd.common.ErrorResponseCode;
import org.gluu.oxd.common.ExpiredObjectType;
import org.gluu.oxd.common.Jackson2;
import org.gluu.oxd.common.params.GetTokensByCodeParams;
import org.gluu.oxd.common.response.GetTokensByCodeResponse;
import org.gluu.oxd.common.response.IOpResponse;
import org.gluu.oxd.server.HttpException;
import org.gluu.oxd.server.Utils;
import org.gluu.oxd.server.service.Rp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        final Rp rp = getRp();
        OpenIdConfigurationResponse discoveryResponse = getDiscoveryService().getConnectDiscoveryResponse(rp);

        final TokenRequest tokenRequest = new TokenRequest(GrantType.AUTHORIZATION_CODE);
        tokenRequest.setCode(params.getCode());
        tokenRequest.setRedirectUri(rp.getRedirectUri());
        tokenRequest.setAuthUsername(rp.getClientId());
        AuthenticationMethod authenticationMethod = Strings.isNullOrEmpty(params.getAuthenticationMethod()) ? AuthenticationMethod.fromString(rp.getTokenEndpointAuthMethod()) : AuthenticationMethod.fromString(params.getAuthenticationMethod());

        if (authenticationMethod == null) {
            LOG.debug("TokenEndpointAuthMethod is either not set or not valid. Setting `client_secret_basic` as AuthenticationMethod. TokenEndpointAuthMethod : {} ", rp.getTokenEndpointAuthMethod());
            tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);
        } else {
            tokenRequest.setAuthenticationMethod(authenticationMethod);
        }

        if (Lists.newArrayList(AuthenticationMethod.PRIVATE_KEY_JWT, AuthenticationMethod.TLS_CLIENT_AUTH, AuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH).contains(authenticationMethod)) {

            Algorithm algorithm = Strings.isNullOrEmpty(params.getAlgorithm()) ? Algorithm.fromString(rp.getTokenEndpointAuthSigningAlg()) : Algorithm.fromString(params.getAlgorithm());
            if (algorithm == null) {
                LOG.error("TokenEndpointAuthSigningAlg is either not set or not valid. TokenEndpointAuthSigningAlg : {} ", rp.getTokenEndpointAuthSigningAlg());
                throw new HttpException(ErrorResponseCode.INVALID_SIGNATURE_ALGORITHM);
            }

            tokenRequest.setAlgorithm(SignatureAlgorithm.fromString(rp.getTokenEndpointAuthSigningAlg()));

            if (!getConfigurationService().getConfiguration().getEnableJwksGeneration()) {
                LOG.error("The Token Authentication Method is {}. Please set `enable_jwks_generation` (to `true`), `crypt_provider_key_store_path` and `crypt_provider_key_store_password` in `oxd-server.yml` to enable RP-jwks generation in oxd.", authenticationMethod.toString());
                throw new HttpException(ErrorResponseCode.JWKS_GENERATION_DISABLE);
            }

            tokenRequest.setCryptoProvider(getKeyGeneratorService().getCryptoProvider());
            tokenRequest.setKeyId(getKeyGeneratorService().getCryptoProvider().getKeyId(getKeyGeneratorService().getKeys(), algorithm, Use.SIGNATURE));
            tokenRequest.setAudience(discoveryResponse.getTokenEndpoint());
        } else {
            tokenRequest.setAuthPassword(rp.getClientSecret());
        }

        final TokenClient tokenClient = getOpClientFactory().createTokenClient(discoveryResponse.getTokenEndpoint());
        tokenClient.setExecutor(getHttpService().getClientEngine());
        tokenClient.setRequest(tokenRequest);
        final TokenResponse response = tokenClient.exec();

        if (response.getStatus() == 200 || response.getStatus() == 302) { // success or redirect

            if (Strings.isNullOrEmpty(response.getIdToken())) {
                LOG.error("id_token is not returned. Please check: 1) OP log file for error (oxauth.log) 2) whether 'openid' scope is present for 'get_authorization_url' command");
                LOG.error("Entity: " + response.getEntity());
                throw new HttpException(ErrorResponseCode.NO_ID_TOKEN_RETURNED);
            }

            if (Strings.isNullOrEmpty(response.getAccessToken())) {
                LOG.error("access_token is not returned");
                throw new HttpException(ErrorResponseCode.NO_ACCESS_TOKEN_RETURNED);
            }

            final Jwt idToken = Jwt.parse(response.getIdToken());

            final Validator validator = new Validator.Builder()
                    .discoveryResponse(discoveryResponse)
                    .idToken(idToken)
                    .keyService(getKeyService())
                    .opClientFactory(getOpClientFactory())
                    .oxdServerConfiguration(getConfigurationService().getConfiguration())
                    .rp(rp)
                    .build();

            String state = getStateService().encodeExpiredObject(params.getState(), ExpiredObjectType.STATE);

            validator.validateNonce(getStateService());
            validator.validateIdToken();
            validator.validateAccessToken(response.getAccessToken());
            validator.validateState(state);
            // persist tokens
            rp.setIdToken(response.getIdToken());
            rp.setAccessToken(response.getAccessToken());
            getRpService().update(rp);
            getStateService().deleteExpiredObjectsByKey(state);

            LOG.trace("Scope: " + response.getScope());

            final GetTokensByCodeResponse opResponse = new GetTokensByCodeResponse();
            opResponse.setAccessToken(response.getAccessToken());
            opResponse.setIdToken(response.getIdToken());
            opResponse.setRefreshToken(response.getRefreshToken());
            opResponse.setExpiresIn(response.getExpiresIn() != null ? response.getExpiresIn() : -1);
            opResponse.setIdTokenClaims(Jackson2.createJsonMapper().readTree(idToken.getClaims().toJsonString()));
            return opResponse;
        } else {
            if (response.getStatus() == 400) {
                throw new HttpException(ErrorResponseCode.BAD_REQUEST_INVALID_CODE);
            }
            LOG.error("Failed to get tokens because response code is: " + response.getScope());
        }
        return null;
    }

    private void validate(GetTokensByCodeParams params) {

        if (Strings.isNullOrEmpty(params.getCode())) {
            throw new HttpException(ErrorResponseCode.BAD_REQUEST_NO_CODE);
        }
        if (Strings.isNullOrEmpty(params.getState())) {
            throw new HttpException(ErrorResponseCode.BAD_REQUEST_NO_STATE);
        }
        try {
            if (!getStateService().isExpiredObjectPresent(getStateService().encodeExpiredObject(params.getState(), ExpiredObjectType.STATE))) {
                throw new HttpException(ErrorResponseCode.BAD_REQUEST_STATE_NOT_VALID);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new HttpException(ErrorResponseCode.BAD_REQUEST_STATE_NOT_VALID);
        }
    }
}
