package org.gluu.oxd.common.params;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.gluu.oxd.common.Jackson2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThirdPartyLoginParams implements HasOxdIdParams {
    private static final Logger LOG = LoggerFactory.getLogger(ThirdPartyLoginParams.class);
    @JsonProperty(value = "oxd_id")
    private String oxd_id;
    @JsonProperty(value = "iss")
    String iss;
    @JsonProperty(value = "login_hint")
    String loginHint;
    @JsonProperty(value = "target_link_uri")
    String targetLinkUri;

    public ThirdPartyLoginParams() {
    }

    public ThirdPartyLoginParams(String oxdId, String iss, String loginHint, String targetLinkUri) {
        this.oxd_id = oxdId;
        this.iss = iss;
        this.loginHint = loginHint;
        this.targetLinkUri = targetLinkUri;
    }

    public String getOxdId() {
        return oxd_id;
    }

    public void setOxd_id(String oxd_id) {
        this.oxd_id = oxd_id;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getLoginHint() {
        return loginHint;
    }

    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }

    public String getTargetLinkUri() {
        return targetLinkUri;
    }

    public void setTargetLinkUri(String targetLinkUri) {
        this.targetLinkUri = targetLinkUri;
    }

    @Override
    public String toString() {
        return "ThirdPartyLoginParams{" +
                "oxd_id='" + oxd_id + '\'' +
                ", iss='" + iss + '\'' +
                ", loginHint='" + loginHint + '\'' +
                ", targetLinkUri='" + targetLinkUri + '\'' +
                '}';
    }

    public String toJsonString() {
        try {
            return Jackson2.serializeWithoutNulls(this);
        } catch (IOException e) {
            LOG.error("Error in parsing StringParam object.", e);
            throw new RuntimeException(e);
        }
    }
}
