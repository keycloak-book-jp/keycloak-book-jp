package sample.clientapp.config;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ConfigurationProperties(prefix = "server.ssl")
public class SslConfiguration {

    private Resource keyStore;
    private String keyStorePassword;
    private String keyPassword;
    private Resource trustStore;
    private String trustStorePassword;

    public Resource getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(Resource keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public Resource getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(Resource trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) throws Exception {

        if (keyStore == null) { // Disable SSL
            RestTemplateBuilder RestTemplateBuilder = new RestTemplateBuilder();
            return RestTemplateBuilder.build();

        } else { // Enable SSL
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadKeyMaterial(keyStore.getURL(), keyStorePassword.toCharArray(), keyPassword.toCharArray())
                    .loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray(), null).build();
            HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
            return builder.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient)).build();
        }
    }
}