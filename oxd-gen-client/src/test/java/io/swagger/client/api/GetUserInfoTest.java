package io.swagger.client.api;

import io.swagger.client.ApiResponse;
import io.swagger.client.model.GetTokensByCodeParams;
import io.swagger.client.model.GetTokensByCodeResponse;
import io.swagger.client.model.GetUserInfoParams;
import io.swagger.client.model.RegisterSiteResponse;
import org.gluu.oxd.common.CoreUtils;
import org.gluu.oxd.common.model.AuthenticationDetails;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Map;

import static io.swagger.client.api.Tester.*;
import static org.testng.Assert.*;


/**
 * @author Yuriy Zabrovarnyy
 * @author Shoeb
 * @version 10/25/2018
 */

public class GetUserInfoTest {

    @Parameters({"opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void test(String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) throws Exception {
        final DevelopersApi client = api();

        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls);
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(null, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        final GetTokensByCodeResponse tokens = requestTokens(client, site, authenticationDetails);

        final GetUserInfoParams params = new GetUserInfoParams();
        params.setOxdId(site.getOxdId());
        params.setAccessToken(tokens.getAccessToken());
        params.setIdToken(tokens.getIdToken());

        final Map<String, Object> resp = client.getUserInfo(params, getAuthorization(site), null);
        assertNotNull(resp);
        assertFalse(resp.isEmpty());
        assertNotNull(resp.get("sub"));
    }

    @Parameters({"opHost", "redirectUrls", "userInum", "userEmail"})
    @Test
    public void testWithInvalidToken(String opHost, String redirectUrls, String userInum, String userEmail) throws Exception {
        final DevelopersApi client = api();

        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls);

        final GetUserInfoParams params = new GetUserInfoParams();
        params.setOxdId(site.getOxdId());
        params.setAccessToken("blahBlah"); // invalid token

        final ApiResponse<Map<String, Object>> apiResponse = client.getUserInfoWithHttpInfo(params, getAuthorization(site), null);
        assertEquals(apiResponse.getStatusCode(), 200); // fixme should be 401

        assertNotNull(apiResponse.getData());
        assertNull(apiResponse.getData().get("sub"));
    }

    private GetTokensByCodeResponse requestTokens(DevelopersApi client, RegisterSiteResponse site, AuthenticationDetails authenticationDetails) throws Exception {

        final GetTokensByCodeParams params = new GetTokensByCodeParams();
        params.setOxdId(site.getOxdId());
        params.setCode(GetTokensByCodeTest.codeRequest(client, authenticationDetails, site.getOxdId(), getAuthorization(site)));
        params.setState(authenticationDetails.getState());

        final GetTokensByCodeResponse resp = client.getTokensByCode(params, getAuthorization(site), null);
        assertNotNull(resp);
        notEmpty(resp.getAccessToken());
        notEmpty(resp.getIdToken());
        return resp;
    }
}
