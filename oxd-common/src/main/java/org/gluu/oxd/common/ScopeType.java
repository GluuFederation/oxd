package org.gluu.oxd.common;

import java.util.HashMap;
import java.util.Map;

public enum ScopeType {
    UMA_PROTECTION("uma_protection"),
    OPENID("openid"),
    ADDRESS("address"),
    CLIENTINFO("clientinfo"),
    EMAIL("email"),
    MOBILE_PHONE("mobile_phone"),
    PERMISSION("permission"),
    PHONE("phone"),
    PROFILE("profile"),
    USER_NAME("user_name"),
    OXD("oxd");

    private static Map<String, ScopeType> lookup = new HashMap();
    private String m_value;

    private ScopeType(String p_value) {
        this.m_value = p_value;
    }

    public String getValue() {
        return this.m_value;
    }

    public static ScopeType fromValue(String p_value) {
        return (ScopeType)lookup.get(p_value);
    }

    static {
        ScopeType[] var0 = values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            ScopeType enumType = var0[var2];
            lookup.put(enumType.getValue(), enumType);
        }

    }
}

