package org.gluu.oxd.server;

import com.fasterxml.jackson.databind.JsonNode;
import org.gluu.oxd.client.ClientInterface;
import org.gluu.oxd.client.GetTokensByCodeResponse2;
import org.gluu.oxd.common.CoreUtils;
import org.gluu.oxd.common.params.GetTokensByCodeParams;
import org.gluu.oxd.common.params.GetUserInfoParams;
import org.gluu.oxd.common.response.RegisterSiteResponse;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static org.gluu.oxd.server.TestUtils.notEmpty;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 12/10/2015
 */

public class GetUserInfoTest {

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret"})
    @Test
    public void test(String host, String opHost, String redirectUrls, String userId, String userSecret) {
        ClientInterface client = Tester.newClient(host);

        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls);
        final GetTokensByCodeResponse2 tokens = requestTokens(client, opHost, site, userId, userSecret, site.getClientId(), redirectUrls);

        GetUserInfoParams params = new GetUserInfoParams();
        params.setOxdId(site.getOxdId());
        params.setAccessToken(tokens.getAccessToken());
        params.setIdToken(tokens.getIdToken());

        final JsonNode resp = client.getUserInfo(Tester.getAuthorization(site), null, params);
        assertNotNull(resp);
        assertNotNull(resp.get("sub"));
    }

    private GetTokensByCodeResponse2 requestTokens(ClientInterface client, String opHost, RegisterSiteResponse site, String userId, String userSecret, String clientId, String redirectUrls) {

        final String state = CoreUtils.secureRandomString();
        final String nonce = CoreUtils.secureRandomString();
        final GetTokensByCodeParams params = new GetTokensByCodeParams();
        params.setOxdId(site.getOxdId());
        params.setCode(GetTokensByCodeTest.codeRequest(client, opHost, site, userId, userSecret, clientId, redirectUrls, state, nonce));
        params.setState(state);

        final GetTokensByCodeResponse2 resp = client.getTokenByCode(Tester.getAuthorization(site), null, params);
        assertNotNull(resp);
        notEmpty(resp.getAccessToken());
        notEmpty(resp.getIdToken());
        return resp;
    }
}
