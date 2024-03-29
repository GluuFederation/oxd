package org.gluu.oxd.server;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxd.client.ClientInterface;
import org.gluu.oxd.client.GetTokensByCodeResponse2;
import org.gluu.oxd.common.CoreUtils;
import org.gluu.oxd.common.model.AuthenticationDetails;
import org.gluu.oxd.common.params.GetTokensByCodeParams;
import org.gluu.oxd.common.params.GetUserInfoParams;
import org.gluu.oxd.common.params.RpGetRptParams;
import org.gluu.oxd.common.response.RegisterSiteResponse;
import org.gluu.oxd.common.response.RpGetRptResponse;
import org.gluu.oxd.common.response.RsCheckAccessResponse;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static org.gluu.oxd.server.TestUtils.notEmpty;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

//Set `protect_commands_with_access_token` field to true in oxd-server.yml file
public class DifferentAuthServerTest {

    @Parameters({"host", "opHost", "authServer", "redirectUrls", "clientId", "clientSecret", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getUserInfo_withDifferentAuthServer(String host, String opHost, String authServer, String redirectUrls, String clientId, String clientSecret, String userId, String userSecret, String userInum, String userEmail) {

        ClientInterface client = org.gluu.oxd.server.Tester.newClient(host);
        RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls);
        AuthenticationDetails authenticationDetails = io.swagger.client.api.TestUtils.setAuthenticationDetails(null, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        RegisterSiteResponse authServerResp = RegisterSiteTest.registerSite(client, authServer, redirectUrls);

        final GetTokensByCodeResponse2 tokens = requestTokens(client, site, authServerResp, authenticationDetails);

        GetUserInfoParams params = new GetUserInfoParams();
        params.setOxdId(site.getOxdId());
        params.setAccessToken(tokens.getAccessToken());
        params.setIdToken(tokens.getIdToken());

        final JsonNode resp = client.getUserInfo(Tester.getAuthorization(authServerResp), authServerResp.getOxdId(), params);
        assertNotNull(resp);
        assertNotNull(resp.get("sub"));
    }

    @Parameters({"host", "authServer", "redirectUrls", "opHost", "rsProtect"})
    @Test
    public void umaFullTest_withDifferentAuthServer(String host, String authServer, String redirectUrls, String opHost, String rsProtect) throws Exception {

        ClientInterface client = Tester.newClient(host);

        RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls);
        RegisterSiteResponse authServerResp = RegisterSiteTest.registerSite(client, authServer, redirectUrls);

        RsProtectTest.protectResources(client, site, UmaFullTest.resourceList(rsProtect).getResources());

        final RsCheckAccessResponse checkAccess = RsCheckAccessTest.checkAccess(client, site, null);

        final RpGetRptParams params = new RpGetRptParams();
        params.setOxdId(site.getOxdId());
        params.setTicket(checkAccess.getTicket());

        final RpGetRptResponse response = client.umaRpGetRpt(Tester.getAuthorization(authServerResp), authServerResp.getOxdId(), params);

        Assert.assertNotNull(response);
        assertTrue(StringUtils.isNotBlank(response.getRpt()));
        assertTrue(StringUtils.isNotBlank(response.getPct()));
    }

    private GetTokensByCodeResponse2 requestTokens(ClientInterface client, RegisterSiteResponse site, RegisterSiteResponse authServer, AuthenticationDetails authServerResp) {

        final GetTokensByCodeParams params = new GetTokensByCodeParams();
        params.setOxdId(site.getOxdId());
        params.setCode(GetTokensByCodeTest.codeRequest(client, site, authServerResp));
        params.setState(authServerResp.getState());

        final GetTokensByCodeResponse2 resp = client.getTokenByCode(Tester.getAuthorization(authServer), authServer.getOxdId(), params);
        assertNotNull(resp);
        notEmpty(resp.getAccessToken());
        notEmpty(resp.getIdToken());
        return resp;
    }
}
