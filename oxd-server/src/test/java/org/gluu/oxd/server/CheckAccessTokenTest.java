package org.gluu.oxd.server;

import org.gluu.oxd.client.ClientInterface;
import org.gluu.oxd.client.GetTokensByCodeResponse2;
import org.gluu.oxd.common.CoreUtils;
import org.gluu.oxd.common.model.AuthenticationDetails;
import org.gluu.oxd.common.params.CheckAccessTokenParams;
import org.gluu.oxd.common.response.CheckAccessTokenResponse;
import org.gluu.oxd.common.response.RegisterSiteResponse;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 23/10/2013
 */

public class CheckAccessTokenTest {
    @Parameters({"host", "redirectUrls", "userId", "userSecret", "opHost", "userInum", "userEmail"})
    @Test
    public void test(String host, String redirectUrls, String userId, String userSecret, String opHost, String userInum, String userEmail) {

        ClientInterface client = Tester.newClient(host);
        String nonce = CoreUtils.secureRandomString();
        String state = CoreUtils.secureRandomString();
        RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls);
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, nonce, state, userInum, userEmail);
        GetTokensByCodeResponse2 response = GetTokensByCodeTest.tokenByCode(client, site, authenticationDetails);

        final CheckAccessTokenParams params = new CheckAccessTokenParams();
        params.setAccessToken(response.getAccessToken());
        params.setIdToken(response.getIdToken());
        params.setOxdId(site.getOxdId());

        final CheckAccessTokenResponse checkR = client.checkAccessToken(Tester.getAuthorization(site), null, params);
        assertNotNull(checkR);
        assertTrue(checkR.isActive());
        assertNotNull(checkR.getExpiresAt());
        assertNotNull(checkR.getIssuedAt());
    }

}
