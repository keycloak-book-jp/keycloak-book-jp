package sample.clientapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth.config")
public class OauthConfiguration {
    private boolean state;
    private boolean nonce;
    private boolean pkce;
    private boolean formPost;

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean isNonce() {
        return nonce;
    }

    public void setNonce(boolean nonce) {
        this.nonce = nonce;
    }

    public boolean isPkce() {
        return pkce;
    }

    public void setPkce(boolean pkce) {
        this.pkce = pkce;
    }

    public boolean isFormPost() {
        return formPost;
    }

    public void setFormPost(boolean formPost) {
        this.formPost = formPost;
    }

}