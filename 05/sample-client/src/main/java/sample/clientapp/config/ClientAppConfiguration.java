package sample.clientapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "clientapp.config")
public class ClientAppConfiguration {
    private String apiserverUrl;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String revokeEndpoint;
    private String clientId;
    private String clientSecret;
    private String scope;

    public String getApiserverUrl() {
        return apiserverUrl;
    }

    public void setApiserverUrl(String value) {
        apiserverUrl = value;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getRevokeEndpoint() {
        return revokeEndpoint;
    }

    public void setRevokeEndpoint(String value) {
        revokeEndpoint = value;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String value) {
        clientId = value;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}