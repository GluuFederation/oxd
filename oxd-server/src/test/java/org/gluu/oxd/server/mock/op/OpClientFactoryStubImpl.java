package org.gluu.oxd.server.mock.op;

import org.assertj.core.util.Lists;
import org.gluu.oxauth.client.*;
import org.gluu.oxauth.model.common.GrantType;
import org.gluu.oxauth.model.common.TokenType;
import org.gluu.oxd.server.op.OpClientFactory;

import java.util.Calendar;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpClientFactoryStubImpl implements OpClientFactory {
    @Override
    public TokenClient createTokenClient(String url) {
        TokenClient client = mock(TokenClient.class);

        TokenResponse response = new TokenResponse();
        response.setAccessToken("DUMMY_ACCESS_TOKEN_"+ System.currentTimeMillis());
        response.setTokenType(TokenType.BEARER);
        response.setExpiresIn(299);
        response.setRefreshToken(null);
        response.setScope("openid");
        response.setIdToken(null);

        when(client.exec()).thenReturn(response);
        when(client.execClientCredentialsGrant(any(), any(), any())).thenReturn(response);

        return client;
    }

    @Override
    public UserInfoClient createUserInfoClient(String url) {
        return null;
    }

    @Override
    public RegisterClient createRegisterClient(String url) {
        RegisterClient client = mock(RegisterClient.class);

        RegisterResponse response = new RegisterResponse();
        response.setClientId("DUMMY_CLIENT_ID_"+ System.currentTimeMillis());
        response.setClientSecret("DUMMY_CLIENT_SECRET_"+ System.currentTimeMillis());
        response.setRegistrationAccessToken("DUMMY_REGISTRATION_ACCESS_TOKEN");
        response.setRegistrationClientUri("https://www.dummy-op-server.xyz/oxauth/restv1/register?client_id=@!8DBF.24EB.FA0E.1BFF!0001!32B7.932A!0008!AB90.6BF3.8E32.7A13");

        Calendar calendar = Calendar.getInstance();
        // get a date to represent "today"
        Date today = calendar.getTime();
        // add one day to the date/calendar
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();

        response.setClientIdIssuedAt(today);
        response.setClientIdIssuedAt(tomorrow);

        when(client.exec()).thenReturn(response);
        return client;
    }

    @Override
    public OpenIdConfigurationClient createOpenIdConfigurationClient(String url) {
        return null;
    }

    @Override
    public AuthorizeClient createAuthorizeClient(String url) {
        return null;
    }
}
