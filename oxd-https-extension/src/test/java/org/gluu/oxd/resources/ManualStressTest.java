package org.gluu.oxd.resources;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxd.RestResource;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xdi.oxd.common.CommandResponse;
import org.xdi.oxd.common.params.GetAuthorizationUrlParams;
import org.xdi.oxd.common.params.GetClientTokenParams;
import org.xdi.oxd.common.params.IParams;
import org.xdi.oxd.common.params.RegisterSiteParams;
import org.xdi.oxd.common.response.GetAuthorizationUrlResponse;
import org.xdi.oxd.common.response.GetClientTokenResponse;
import org.xdi.oxd.common.response.IOpResponse;
import org.xdi.oxd.common.response.SetupClientResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import java.io.IOException;

import static org.testng.AssertJUnit.assertTrue;


/**
 * @author yuriyz
 */
public class ManualStressTest {

    private static int HTTP_PORT = 8088;
    private static RegisterSiteParams registerSiteParams;
    private static String opHost = "https://ce-dev3.gluu.org";
    private static String authorizationRedirectUrl = "https://client.example.com/cb";
    private static String userID = "test_user";
    private static String userSecret = "test_user_password";

    private static int oxdPort = 8099;
    private static String oxdHost = "localhost";
    private static String accessToken = null;
    private static String oxdId = null;

    private static String clientId = null;
    private static String clientSecret = null;
//    private static AtomicInteger counter = new AtomicInteger();


    @BeforeClass
    public static void beforeClass() throws Exception {
        final Client client = ClientBuilder.newClient();
        try {
            registerSiteParams = new RegisterSiteParams();
            registerSiteParams.setOpHost(opHost);
            registerSiteParams.setAuthorizationRedirectUri(authorizationRedirectUrl);
            registerSiteParams.setScope(Lists.newArrayList("openid", "profile", "email", "uma_protection"));  //Scope
            registerSiteParams.setTrustedClient(true);
            registerSiteParams.setGrantType(Lists.newArrayList("authorization_code", "client_credentials"));

            //Get AccessToken
            SetupClientResponse setupClientResponse = httpClient(client, "setup-client", registerSiteParams, SetupClientResponse.class);

            GetClientTokenParams clientTokenParams = new GetClientTokenParams();
            clientTokenParams.setClientId(setupClientResponse.getClientId());
            clientTokenParams.setClientSecret(setupClientResponse.getClientSecret());
            clientTokenParams.setScope(Lists.newArrayList("openid", "profile", "email", "uma_protection"));
            clientTokenParams.setOpHost(opHost);

            GetClientTokenResponse clientTokenResponse = httpClient(client, "get-client-token", clientTokenParams, GetClientTokenResponse.class);

            accessToken = clientTokenResponse.getAccessToken();
            clientId = setupClientResponse.getClientId();
            clientSecret = setupClientResponse.getClientSecret();

            assertTrue(StringUtils.isNotBlank(accessToken));
            assertTrue(StringUtils.isNotBlank(clientId));
            assertTrue(StringUtils.isNotBlank(clientSecret));

            oxdId = setupClientResponse.getOxdId();
        } finally {
            client.close();
        }
    }

    @AfterClass
    public static void afterClass() {
    }

    @Test(invocationCount = 1000, threadPoolSize = 30, enabled = false)
    public void manualStress() throws IOException {
        final Client client = ClientBuilder.newClient();
        try {
            Assert.assertNotNull(accessToken);
            Assert.assertNotNull(oxdId);

            GetAuthorizationUrlParams params = new GetAuthorizationUrlParams();
            params.setOxdId(oxdId);

            GetAuthorizationUrlResponse getAuthorizationUrlResponse = httpClient(client, "get-authorization-url", params, GetAuthorizationUrlResponse.class);
            ;
            Assert.assertNotNull(getAuthorizationUrlResponse);
            //RestResourceTest.output("GET AUTHORIZATION URL", getAuthorizationUrlResponse);

            GetClientTokenParams clientTokenParams = new GetClientTokenParams();
            clientTokenParams.setClientId(clientId);
            clientTokenParams.setClientSecret(clientSecret);
            clientTokenParams.setScope(Lists.newArrayList("openid", "profile", "email", "uma_protection"));
            clientTokenParams.setOpHost(opHost);

            GetClientTokenResponse clientTokenResponse = httpClient(client, "get-client-token", clientTokenParams, GetClientTokenResponse.class);
            accessToken = clientTokenResponse.getAccessToken();
            //System.out.println(counter.incrementAndGet());
        } finally {
            client.close();
        }
    }

    public static CommandResponse httpClient(Client client, String endpoint, IParams params) throws IOException {
        final String entity = client.target("http://localhost:" + HTTP_PORT + "/" + endpoint)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .post(Entity.json(RestResourceTest.getParameterJson(params)))
                .readEntity(String.class);

        //System.out.println("Plain string: " + entity);
        return RestResource.read(entity, CommandResponse.class);
    }

    public static <T extends IOpResponse> T httpClient(Client client, String endpoint, IParams params, Class<T> responseClazz) throws IOException {
        CommandResponse commandResponse = httpClient(client, endpoint, params);
        return RestResource.read(commandResponse.getData().toString(), responseClazz);
    }
}
