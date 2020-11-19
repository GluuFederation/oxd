package org.gluu.oxd.server.op;

import com.google.inject.Injector;
import org.gluu.oxd.common.Command;
import org.gluu.oxd.common.params.ThirdPartyLoginParams;
import org.gluu.oxd.common.response.GetAuthorizationUrlResponse;
import org.gluu.oxd.common.response.IOpResponse;
import org.gluu.oxd.server.Utils;
import org.gluu.oxd.server.service.Rp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitiateThirdPartyLoginOperation extends BaseOperation<ThirdPartyLoginParams> {

    private static final Logger LOG = LoggerFactory.getLogger(InitiateThirdPartyLoginOperation.class);

    protected InitiateThirdPartyLoginOperation(Command command, Injector injector) {
        super(command, injector, ThirdPartyLoginParams.class);
    }

    @Override
    public IOpResponse execute(ThirdPartyLoginParams params) throws Exception {
        final Rp rp = getRp();
        String authorizationEndpoint = getDiscoveryService().getConnectDiscoveryResponse(rp).getAuthorizationEndpoint();

        String state = getStateService().generateState();
        String nonce = getStateService().generateNonce();
        String clientId = getConfigurationService().getConfiguration().getEncodeClientIdInAuthorizationUrl() ? Utils.encode(rp.getClientId()) : rp.getClientId();
        String redirectUri = rp.getRedirectUri();

        authorizationEndpoint += "?response_type=" + Utils.joinAndUrlEncode(rp.getResponseTypes());
        authorizationEndpoint += "&client_id=" + clientId;
        authorizationEndpoint += "&redirect_uri=" + redirectUri;
        authorizationEndpoint += "&scope=" + Utils.joinAndUrlEncode(rp.getScope());
        authorizationEndpoint += "&state=" + state;
        authorizationEndpoint += "&nonce=" + nonce;

        return new GetAuthorizationUrlResponse(authorizationEndpoint);
    }
}
