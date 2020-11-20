package org.gluu.oxd.server;

import org.gluu.oxd.client.ClientInterface;
import org.gluu.oxd.common.response.GetAuthorizationUrlResponse;
import org.gluu.oxd.common.response.RegisterSiteResponse;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertNotNull;

public class ThirdPartyLoginTest {

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret"})
    @Test
    public void test(String host, String opHost, String redirectUrls, String userId, String userSecret) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite_withInitiateLoginUri(client, opHost, redirectUrls, "https://localhost" + ":" + SetUpTest.SUPPORT.getLocalPort() + "/initiate-third-party-login");
        GetAuthorizationUrlResponse authzUrl = client.initiateThirdPartyLogin(Tester.getAuthorization(site), null, site.getOxdId(), opHost, null, null);
        assertNotNull(authzUrl.getAuthorizationUrl());

    }
}
