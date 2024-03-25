package io.swagger.client.api;

import org.gluu.oxd.common.model.AuthenticationDetails;

public class TestUtils {
    public static AuthenticationDetails setAuthenticationDetails(String host, String opHost, String userId, String userSecret, String clientId, String redirectUrls, String nonce, String state, String userInum, String userEmail) {
        AuthenticationDetails authenticationDetails = new AuthenticationDetails();
        authenticationDetails.setHost(host);
        authenticationDetails.setNonce(nonce);
        authenticationDetails.setOpHost(opHost);
        authenticationDetails.setRedirectUrls(redirectUrls);
        authenticationDetails.setState(state);
        authenticationDetails.setUserId(userId);
        authenticationDetails.setUserInum(userInum);
        authenticationDetails.setClientId(clientId);
        authenticationDetails.setUserSecret(userSecret);
        authenticationDetails.setUserEmail(userEmail);
        return authenticationDetails;
    }
}
