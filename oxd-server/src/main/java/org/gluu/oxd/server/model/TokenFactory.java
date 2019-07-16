package org.gluu.oxd.server.model;

import org.gluu.oxauth.model.uma.UmaScopeType;

public class TokenFactory {
    private TokenFactory() {
    }

    public static Token newToken() {
            return new Pat();
    }
}
