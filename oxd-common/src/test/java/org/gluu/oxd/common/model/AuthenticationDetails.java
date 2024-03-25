package org.gluu.oxd.common.model;

public class AuthenticationDetails {
    private String host;
    private String opHost;
    private String redirectUrls;
    private String userId;
    private String userSecret;
    private String userInum;
    private String userEmail;
    private String state;
    private String nonce;
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getOpHost() {
        return opHost;
    }

    public void setOpHost(String opHost) {
        this.opHost = opHost;
    }

    public String getRedirectUrls() {
        return redirectUrls;
    }

    public void setRedirectUrls(String redirectUrls) {
        this.redirectUrls = redirectUrls;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserSecret() {
        return userSecret;
    }

    public void setUserSecret(String userSecret) {
        this.userSecret = userSecret;
    }

    public String getUserInum() {
        return userInum;
    }

    public void setUserInum(String userInum) {
        this.userInum = userInum;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "AuthenticationDetails{" +
                "host='" + host + '\'' +
                ", opHost='" + opHost + '\'' +
                ", redirectUrls='" + redirectUrls + '\'' +
                ", userId='" + userId + '\'' +
                ", userSecret='" + userSecret + '\'' +
                ", userInum='" + userInum + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", state='" + state + '\'' +
                ", nonce='" + nonce + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
