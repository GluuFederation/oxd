package org.xdi.oxd.client.manual;

import org.xdi.oxd.client.CommandClient;
import org.xdi.oxd.common.Command;
import org.xdi.oxd.common.CommandType;
import org.xdi.oxd.common.params.ImplicitFlowParams;
import org.xdi.oxd.common.response.ImplicitFlowResponse;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;
import static org.xdi.oxd.client.TestUtils.notEmpty;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 21/07/2016
 */

public class ImplicitOpTest {

    public static void main(String[] args) throws IOException {
        CommandClient client = null;
        try {
            client = new CommandClient("localhost", 8099);

            final ImplicitFlowParams commandParams = new ImplicitFlowParams();
            commandParams.setClientId("@!2920.FAD2.FB38.9BC5!0001!9227.0E3D!0008!9EC6.7B6C");
            commandParams.setClientSecret("test");
            commandParams.setDiscoveryUrl("https://sso.lobosstudios.com/.well-known/openid-configuration");
            commandParams.setNonce("44693e46-f29a-4148-9f08-9851f87abb37");
            commandParams.setRedirectUrl("https://360.lobosstudios.com/callback");
            commandParams.setScope("openid");
            commandParams.setUserId("test");
            commandParams.setUserSecret("test");

            final Command command = new Command(CommandType.IMPLICIT_FLOW);
            command.setParamsObject(commandParams);

            final ImplicitFlowResponse resp = client.send(command).dataAsResponse(ImplicitFlowResponse.class);
            assertNotNull(resp);

            notEmpty(resp.getAccessToken());
            notEmpty(resp.getAuthorizationCode());
            notEmpty(resp.getIdToken());
            notEmpty(resp.getRefreshToken());
            notEmpty(resp.getScope());
        } finally {
            CommandClient.closeQuietly(client);
        }
    }
}
