package org.gluu.oxd.server.op;

import org.gluu.oxauth.client.*;

public interface OpClientFactory {
    public TokenClient createTokenClient(String url);

    public UserInfoClient createUserInfoClient(String url);

    public RegisterClient createRegisterClient(String url);

    public OpenIdConfigurationClient createOpenIdConfigurationClient(String url);

    public AuthorizeClient createAuthorizeClient(String url);
}
