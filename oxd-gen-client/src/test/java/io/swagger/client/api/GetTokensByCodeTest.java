package io.swagger.client.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import io.swagger.client.model.*;
import org.gluu.oxauth.model.common.AuthenticationMethod;
import org.gluu.oxd.common.CoreUtils;
import org.gluu.oxd.common.SeleniumTestUtils;
import org.gluu.oxd.common.model.AuthenticationDetails;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.util.Strings;

import static io.swagger.client.api.Tester.notEmpty;
import static org.testng.Assert.assertNotNull;

/**
 * Test class to test refresh token and related end points
 *
 * @author Yuriy Z
 * @author Shoeb
 * @version 5, Oct, 2018
 */
public class GetTokensByCodeTest {

    private static final String AUTH_CODE_ENDPOINT = "/get-authorization-code";

    @Parameters({"opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void test(String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) throws Exception {

        DevelopersApi client = Tester.api();

        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls);
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(null, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse tokensResponse = tokenByCode(client, site, authenticationDetails);

        refreshToken(tokensResponse, client, site);
    }

    @Parameters({"opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void withAuthenticationMethod_shouldGetTokenInResponse(String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) throws Exception {

        DevelopersApi client = Tester.api();

        final RegisterSiteResponse site = RegisterSiteTest.registerSite_withAuthenticationMethod(client, opHost, redirectUrls, redirectUrls, redirectUrls, "PS256", AuthenticationMethod.PRIVATE_KEY_JWT.toString());
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(null, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse tokensResponse = tokenByCode(client, site,  authenticationDetails, AuthenticationMethod.PRIVATE_KEY_JWT.toString(), "PS256");

    }

    private static void refreshToken(GetTokensByCodeResponse resp, DevelopersApi client, RegisterSiteResponse site) throws Exception {
        notEmpty(resp.getRefreshToken());

        final String authorization = Tester.getAuthorization(site);

        // refresh token
        final GetAccessTokenByRefreshTokenParams refreshParams = new GetAccessTokenByRefreshTokenParams();
        refreshParams.setOxdId(site.getOxdId());
        refreshParams.setScope(Lists.newArrayList("openid"));
        refreshParams.setRefreshToken(resp.getRefreshToken());

        GetAccessTokenByRefreshTokenResponse refreshResponse = client.getAccessTokenByRefreshToken(refreshParams, authorization, null);

        assertNotNull(refreshResponse);
        notEmpty(refreshResponse.getAccessToken());
        notEmpty(refreshResponse.getRefreshToken());
    }

    private static GetTokensByCodeResponse tokenByCode(DevelopersApi client, RegisterSiteResponse site, AuthenticationDetails authenticationDetails) throws Exception {
        return tokenByCode(client, site, authenticationDetails, null, null);
    }

    private static GetTokensByCodeResponse tokenByCode(DevelopersApi client, RegisterSiteResponse site, AuthenticationDetails authenticationDetails, String authenticationMethod, String algorithm) throws Exception {

        final RegisterSiteResponse authServer = RegisterSiteTest.registerSite(client, authenticationDetails.getOpHost(), authenticationDetails.getRedirectUrls());
        final String authorizationStr = Tester.getAuthorization(authServer);

        final String code = codeRequest(client, authenticationDetails, site.getOxdId(), authorizationStr, authServer.getOxdId());

        notEmpty(code);

        final GetTokensByCodeParams params = new GetTokensByCodeParams();
        params.setOxdId(site.getOxdId());
        params.setCode(code);
        params.setState(authenticationDetails.getState());
        params.setAuthenticationMethod(authenticationMethod);
        params.setAlgorithm(algorithm);

        final GetTokensByCodeResponse resp = client.getTokensByCode(params, authorizationStr, authServer.getOxdId());
        assertNotNull(resp);
        notEmpty(resp.getAccessToken());
        notEmpty(resp.getIdToken());
        notEmpty(resp.getRefreshToken());
        return resp;
    }

    public static String codeRequest(DevelopersApi client, AuthenticationDetails authenticationDetails, String oxdId, String authorization) throws Exception {
        return codeRequest(client, authenticationDetails, oxdId, authorization, null);
    }

    public static String codeRequest(DevelopersApi client, AuthenticationDetails authenticationDetails, String oxdId, String authorization, String authorizationOxdId) throws Exception {
        SeleniumTestUtils.authorizeClient(authenticationDetails, null, null);

        final Request request = buildRequest(authorization, authorizationOxdId, oxdId, authenticationDetails, client);

        final Response response = client.getApiClient().getHttpClient().newCall(request).execute();

        final JsonElement jsonResponse = new JsonParser().parse(response.body().string());

        return jsonResponse.getAsJsonObject().get("code").getAsString();

    }

    private static Request buildRequest(String authorization, String authorizationOxdId, String oxdId, AuthenticationDetails authenticationDetails, DevelopersApi client) {

        final String json = "{\"oxd_id\":\"" + oxdId + "\",\"username\":\"" + authenticationDetails.getUserId() + "\",\"password\":\"" + authenticationDetails.getUserSecret()
                + "\",\"state\":\"" + authenticationDetails.getState() + "\",\"nonce\":\"" + authenticationDetails.getNonce() + "\"}";

        final RequestBody reqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);

        com.squareup.okhttp.Request.Builder request = new Request.Builder();
        if (!Strings.isNullOrEmpty(authorizationOxdId)) {
            request.addHeader("AuthorizationOxdId", authorizationOxdId);
        }
        return request
                .addHeader("Authorization", authorization)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .method("POST", reqBody)
                .url(client.getApiClient().getBasePath() + AUTH_CODE_ENDPOINT).build();
    }

}
