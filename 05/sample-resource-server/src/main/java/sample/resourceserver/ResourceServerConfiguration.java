package sample.resourceserver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "resourceserver.config")
public class ResourceServerConfiguration {
    private String authserverUrl;
    private String introspectionEndpoint;
    private String clientId;
    private String clientSecret;

    public String getAuthserverUrl() {
        return authserverUrl;
    }

    public void setAuthserverUrl(String value) {
        authserverUrl = value;
    }

    public String getIntrospectionEndpoint() {
        return introspectionEndpoint;
    }

    public void setIntrospectionEndpoint(String introspectionEndpoint) {
        this.introspectionEndpoint = introspectionEndpoint;
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
}