package org.xdi.oxd.server;

import com.google.common.collect.Lists;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xdi.oxd.client.ClientInterface;
import org.xdi.oxd.common.CoreUtils;
import org.xdi.oxd.common.params.GetAccessTokenByRefreshTokenParams;
import org.xdi.oxd.common.params.GetAuthorizationCodeParams;
import org.xdi.oxd.common.params.GetTokensByCodeParams;
import org.xdi.oxd.common.response.GetClientTokenResponse;
import org.xdi.oxd.common.response.GetTokensByCodeResponse;
import org.xdi.oxd.common.response.RegisterSiteResponse;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.xdi.oxd.common.ErrorResponseCode.*;
import static org.xdi.oxd.server.TestUtils.assertErrorResponse;
import static org.xdi.oxd.server.TestUtils.notEmpty;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 06/10/2015
 */

public class GetTokensByCodeTest {

    @Parameters({"host", "opHost", "redirectUrl", "userId", "userSecret"})
    @Test
    public void test(String host, String opHost, String redirectUrl, String userId, String userSecret) throws IOException {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrl);
        GetTokensByCodeResponse tokensResponse = tokenByCode(client, site, userId, userSecret, CoreUtils.secureRandomString());
        refreshToken(tokensResponse, client, site.getOxdId());
    }

    public static GetClientTokenResponse refreshToken(GetTokensByCodeResponse resp, ClientInterface client, String oxdId) {
        notEmpty(resp.getRefreshToken());

        // refresh token
        final GetAccessTokenByRefreshTokenParams refreshParams = new GetAccessTokenByRefreshTokenParams();
        refreshParams.setOxdId(oxdId);
        refreshParams.setScope(Lists.newArrayList("openid"));
        refreshParams.setRefreshToken(resp.getRefreshToken());
        refreshParams.setProtectionAccessToken(Tester.getAuthorization());

        GetClientTokenResponse refreshResponse = client.getAccessTokenByRefreshToken(Tester.getAuthorization(), refreshParams);

        assertNotNull(refreshResponse);
        notEmpty(refreshResponse.getAccessToken());
        notEmpty(refreshResponse.getRefreshToken());
        return refreshResponse;
    }

    public static GetTokensByCodeResponse tokenByCode(ClientInterface client, RegisterSiteResponse site, String userId, String userSecret, String nonce) {

        final String state = CoreUtils.secureRandomString();

        String code = codeRequest(client, site.getOxdId(), userId, userSecret, state, nonce);

        notEmpty(code);

        final GetTokensByCodeParams params = new GetTokensByCodeParams();
        params.setOxdId(site.getOxdId());
        params.setCode(code);
        params.setState(state);

        final GetTokensByCodeResponse resp = client.getTokenByCode(Tester.getAuthorization(), params);
        assertNotNull(resp);
        notEmpty(resp.getAccessToken());
        notEmpty(resp.getIdToken());
        notEmpty(resp.getRefreshToken());
        return resp;
    }

    @Parameters({"host", "opHost", "redirectUrl", "userId", "userSecret"})
    @Test
    public void testWithInvalidAuthorizationCode(String host, String opHost, String redirectUrl, String userId, String userSecret) throws Exception {

        final ClientInterface client = Tester.newClient(host);

        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrl);

        final String state = CoreUtils.secureRandomString();

        final String authorizationStr = Tester.getAuthorization();

        final String code = codeRequest(client, site.getOxdId(), userId, userSecret, state, CoreUtils.secureRandomString());

        final String invalidCode = code.concat("blah");

        final GetTokensByCodeParams params = new GetTokensByCodeParams();
        params.setOxdId(site.getOxdId());
        params.setCode(invalidCode);
        params.setState(state);

        try {
            client.getTokenByCode(authorizationStr, params);
        } catch (Exception e) {
            assertErrorResponse(400, INVALID_AUTHORIZATION_CODE_BAD_CODE, e);
        }
    }

    @Parameters({"host", "opHost", "redirectUrl"})
    @Test
    public void testGetTokenWithInvalidState(String host, String opHost, String redirectUrl) throws Exception {

        final ClientInterface client = Tester.newClient(host);

        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrl);

        final String invalidState = CoreUtils.secureRandomString(); // not being registered, so invalid

        final String authorizationStr = Tester.getAuthorization();

        final String code = CoreUtils.secureRandomString();

        final GetTokensByCodeParams params = new GetTokensByCodeParams();
        params.setOxdId(site.getOxdId());
        params.setCode(code);
        params.setState(invalidState);

        try {
            client.getTokenByCode(authorizationStr, params);
        } catch (Exception e) {
            assertErrorResponse(400, BAD_REQUEST_STATE_NOT_VALID, e);
        }
    }

    @Test
    @Parameters({"host", "opHost", "redirectUrl"})
    public void testInvalidRefreshToken(String host, String opHost, String redirectUrl) throws Exception {

        final ClientInterface client = Tester.newClient(host);

        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrl);

        final String authorizationStr = Tester.getAuthorization();

        final String invalidCode = CoreUtils.secureRandomString();

        final GetAccessTokenByRefreshTokenParams params = new GetAccessTokenByRefreshTokenParams();
        params.setOxdId(site.getOxdId());
        params.setRefreshToken(invalidCode);

        try {
            client.getAccessTokenByRefreshToken(authorizationStr, params);
        } catch (Exception ex) {
            assertErrorResponse(401, INVALID_REFRESH_TOKEN, ex);
        }

    }

    public static String codeRequest(ClientInterface client, String siteId, String userId, String userSecret, String state, String nonce) {
        GetAuthorizationCodeParams params = new GetAuthorizationCodeParams();
        params.setOxdId(siteId);
        params.setUsername(userId);
        params.setPassword(userSecret);
        params.setState(state);
        params.setNonce(nonce);

        return client.getAuthorizationCode(Tester.getAuthorization(), params).getCode();
    }

}
