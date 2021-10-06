package sample.clientapp;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import sample.clientapp.jwt.AccessToken;
import sample.clientapp.jwt.IdToken;
import sample.clientapp.jwt.JsonWebToken;
import sample.clientapp.jwt.RefreshToken;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ClientSession {
    private IdToken idToken;
    private AccessToken accessToken;
    private RefreshToken refreshToken;

    private String state;
    private String nonce;
    private String scope;
    private String codeVerifier;

    public void setTokensFromTokenResponse(TokenResponse response) {
        this.accessToken = JsonWebToken.parse(response.getAccessToken(), AccessToken.class);
        this.refreshToken = JsonWebToken.parse(response.getRefreshToken(), RefreshToken.class);
        this.idToken = JsonWebToken.parse(response.getIdToken(), IdToken.class);
    }

    public IdToken getIdToken() {
        return idToken;
    }

    public void setIdToken(IdToken idToken) {
        this.idToken = idToken;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

}
