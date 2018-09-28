package org.xdi.oxd.server;

import com.google.common.collect.Lists;
import io.swagger.client.api.ProtectionAccessTokenRequired;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xdi.oxauth.model.common.IntrospectionResponse;
import org.xdi.oxd.client.ClientInterface;
import org.xdi.oxd.common.ErrorResponse;
import org.xdi.oxd.common.params.GetClientTokenParams;
import org.xdi.oxd.common.params.IntrospectAccessTokenParams;
import org.xdi.oxd.common.response.GetClientTokenResponse;
import org.xdi.oxd.common.response.RegisterSiteResponse;

import static junit.framework.Assert.*;
import static org.xdi.oxd.common.ErrorResponseCode.INVALID_ACCESS_TOKEN_MISMATCHED_CLIENT_ID;
import static org.xdi.oxd.server.TestUtils.notEmpty;

/**
 * @author yuriyz
 */
public class IntrospectAccessTokenTest {

    @Parameters({"host", "opHost", "redirectUrl"})
    @Test
    public void introspectAccessToken(String host, String opHost, String redirectUrl) {

        ClientInterface client = Tester.newClient(host);

        RegisterSiteResponse setupResponse = SetupClientTest.setupClient(client, opHost, redirectUrl);

        final GetClientTokenParams params = new GetClientTokenParams();
        params.setOpHost(opHost);
        params.setScope(Lists.newArrayList("openid"));
        params.setClientId(setupResponse.getClientId());
        params.setClientSecret(setupResponse.getClientSecret());

        GetClientTokenResponse tokenResponse = client.getClientToken(params).dataAsResponse(GetClientTokenResponse.class);

        assertNotNull(tokenResponse);
        notEmpty(tokenResponse.getAccessToken());

        IntrospectAccessTokenParams introspectParams = new IntrospectAccessTokenParams();
        introspectParams.setOxdId(setupResponse.getOxdId());
        introspectParams.setAccessToken(tokenResponse.getAccessToken());

        IntrospectionResponse introspectionResponse = client.introspectAccessToken("Bearer " + tokenResponse.getAccessToken(), introspectParams).dataAsResponse(IntrospectionResponse.class);
        assertNotNull(introspectionResponse);
        assertTrue(introspectionResponse.isActive());

        final Integer issuedAt = introspectionResponse.getIssuedAt();
        assertNotNull(issuedAt);
        Integer expiresAt = introspectionResponse.getExpiresAt();
        assertNotNull(expiresAt);
        assertTrue(expiresAt >= issuedAt);
        //todo : add check for nbf when ready

    }

    @Parameters({"host", "opHost", "redirectUrl"})
    @Test
    @ProtectionAccessTokenRequired
    public void testWithValidTokenDifferentClient(String host, String opHost, String redirectUrl) {

        ClientInterface client = Tester.newClient(host);
        RegisterSiteResponse setupResponse = SetupClientTest.setupClient(client, opHost, redirectUrl);

        final RegisterSiteResponse differentSiteSetup = Tester.getSetupData();

        final GetClientTokenParams params = new GetClientTokenParams();
        params.setOpHost(opHost);
        params.setScope(Lists.newArrayList("openid", "oxd"));
        params.setClientId(differentSiteSetup.getClientId());
        params.setClientSecret(differentSiteSetup.getClientSecret());

        GetClientTokenResponse tokenResponse = client.getClientToken(params).dataAsResponse(GetClientTokenResponse.class);
        IntrospectAccessTokenParams iatParams = new IntrospectAccessTokenParams();
        iatParams.setAccessToken(tokenResponse.getAccessToken());
        iatParams.setOxdId(setupResponse.getOxdId());

        ErrorResponse introspectionResponse = client.introspectAccessToken("Bearer " + tokenResponse.getAccessToken(), iatParams).dataAsResponse(ErrorResponse.class);
        assertNotNull(introspectionResponse);
        assertEquals(introspectionResponse.getError(), INVALID_ACCESS_TOKEN_MISMATCHED_CLIENT_ID.getCode());

    }
}
