package org.gluu.oxd.server;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.codec.binary.Base64;
import org.gluu.oxauth.model.common.AuthenticationMethod;
import org.gluu.oxauth.model.util.Util;
import org.gluu.oxd.client.ClientInterface;
import org.gluu.oxd.client.GetTokensByCodeResponse2;
import org.gluu.oxd.common.CoreUtils;
import org.gluu.oxd.common.SeleniumTestUtils;
import org.gluu.oxd.common.model.AuthenticationDetails;
import org.gluu.oxd.common.params.GetAccessTokenByRefreshTokenParams;
import org.gluu.oxd.common.params.GetAuthorizationCodeParams;
import org.gluu.oxd.common.params.GetTokensByCodeParams;
import org.gluu.oxd.common.response.GetClientTokenResponse;
import org.gluu.oxd.common.response.RegisterSiteResponse;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import javax.ws.rs.BadRequestException;

import static org.gluu.oxd.server.TestUtils.notEmpty;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 06/10/2015
 */

public class GetTokensByCodeTest {

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void whenValidCodeIsUsed_shouldGetTokenInResponse(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls);
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
        refreshToken(tokensResponse, client, site);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void withbase64urlencodeState_shouldGetTokenInResponse(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) throws Exception{
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls);
        String state = Base64.encodeBase64String(Util.getBytes("https://www.gluu,org"));
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
        refreshToken(tokensResponse, client, site);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void withAuthenticationMethod_shouldGetTokenInResponse(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite_withAuthenticationMethod(client, opHost, redirectUrls, "PS256", AuthenticationMethod.PRIVATE_KEY_JWT.toString());
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withHS256(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "HS256");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withHS384(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "HS384");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withHS512(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "HS512");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withRS256(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "RS256");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withRS384(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "RS384");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withRS512(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "RS512");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withES256(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "ES256");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withES384(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "ES384");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withES512(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "ES512");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withPS256(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "PS256");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withPS384(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "PS384");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withPS512(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "PS512");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void getToken_withNoneAlgo(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls, "none");
        AuthenticationDetails authenticationDetails = TestUtils.setAuthenticationDetails(host, opHost, userId, userSecret, site.getClientId(), redirectUrls, CoreUtils.secureRandomString(), CoreUtils.secureRandomString(), userInum, userEmail);
        GetTokensByCodeResponse2 tokensResponse = tokenByCode(client, site, authenticationDetails);
    }

    @Parameters({"host", "opHost", "redirectUrls", "userId", "userSecret", "userInum", "userEmail"})
    @Test
    public void whenInvalidCodeIsUsed_shouldGet400BadRequest(String host, String opHost, String redirectUrls, String userId, String userSecret, String userInum, String userEmail) {
        ClientInterface client = Tester.newClient(host);
        final RegisterSiteResponse site = RegisterSiteTest.registerSite(client, opHost, redirectUrls);
        tokenByInvalidCode(client, site, userId, userSecret, CoreUtils.secureRandomString());
    }

    public static GetClientTokenResponse refreshToken(GetTokensByCodeResponse2 resp, ClientInterface client, RegisterSiteResponse site) {
        notEmpty(resp.getRefreshToken());

        // refresh token
        final GetAccessTokenByRefreshTokenParams refreshParams = new GetAccessTokenByRefreshTokenParams();
        refreshParams.setOxdId(site.getOxdId());
        refreshParams.setScope(Lists.newArrayList("openid", "oxd"));
        refreshParams.setRefreshToken(resp.getRefreshToken());

        GetClientTokenResponse refreshResponse = client.getAccessTokenByRefreshToken(Tester.getAuthorization(site), null, refreshParams);

        assertNotNull(refreshResponse);
        notEmpty(refreshResponse.getAccessToken());
        notEmpty(refreshResponse.getRefreshToken());
        return refreshResponse;
    }

    public static GetTokensByCodeResponse2 tokenByCode(ClientInterface client, RegisterSiteResponse site, AuthenticationDetails authenticationDetails) {
        return tokenByCode(client, site, authenticationDetails, null, null);
    }

    public static GetTokensByCodeResponse2 tokenByCode(ClientInterface client, RegisterSiteResponse site, AuthenticationDetails authenticationDetails, String authenticationMethod, String algorithm) {

        RegisterSiteResponse authServer = RegisterSiteTest.registerSite(client, authenticationDetails.getOpHost(), authenticationDetails.getRedirectUrls());
        String accessToken = Tester.getAuthorization(authServer);
        String authorizationOxdId = authServer.getOxdId();

        String code = codeRequest(client, site, authenticationDetails, accessToken, authorizationOxdId);

        notEmpty(code);

        final GetTokensByCodeParams params = new GetTokensByCodeParams();
        params.setOxdId(site.getOxdId());
        params.setCode(code);
        params.setState(authenticationDetails.getState());
        params.setAuthenticationMethod(authenticationMethod);
        params.setAlgorithm(algorithm);

        final GetTokensByCodeResponse2 resp = client.getTokenByCode(accessToken, authorizationOxdId, params);
        assertNotNull(resp);
        notEmpty(resp.getAccessToken());
        notEmpty(resp.getIdToken());
        //notEmpty(resp.getRefreshToken());
        return resp;
    }

    public static GetTokensByCodeResponse2 tokenByInvalidCode(ClientInterface client, RegisterSiteResponse site, String userId, String userSecret, String nonce) {

        final String state = CoreUtils.secureRandomString();
        //codeRequest(client, site.getOxdId(), userId, userSecret, state, nonce);

        final String code = CoreUtils.secureRandomString();

        String testOxdId = site.getOxdId();

        final GetTokensByCodeParams params = new GetTokensByCodeParams();
        params.setOxdId(testOxdId);
        params.setCode(code);
        params.setState(state);

        GetTokensByCodeResponse2 resp = null;

        try {
            resp = client.getTokenByCode(Tester.getAuthorization(site), null, params);
            assertNotNull(resp);
            notEmpty(resp.getAccessToken());
            notEmpty(resp.getIdToken());
            notEmpty(resp.getRefreshToken());

        } catch (BadRequestException ex) {
            assertEquals(ex.getMessage(), "HTTP 400 Bad Request");
        }

        return resp;
    }

    public static String codeRequest(ClientInterface client,RegisterSiteResponse site, AuthenticationDetails authenticationDetails) {
        return codeRequest(client, site, authenticationDetails, null, null);
    }

    public static String codeRequest(ClientInterface client, RegisterSiteResponse site, AuthenticationDetails authenticationDetails, String accessToken, String authorizationOxdId) {
        SeleniumTestUtils.authorizeClient(authenticationDetails, null, null);
        GetAuthorizationCodeParams params = new GetAuthorizationCodeParams();
        params.setOxdId(site.getOxdId());
        params.setUsername(authenticationDetails.getUserId());
        params.setPassword(authenticationDetails.getUserSecret());
        params.setState(authenticationDetails.getState());
        params.setNonce(authenticationDetails.getNonce());
        accessToken = Strings.isNullOrEmpty(accessToken) ? Tester.getAuthorization(site) : accessToken;

        return client.getAuthorizationCode(accessToken, authorizationOxdId, params).getCode();
    }
}
