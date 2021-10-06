package sample.clientapp.jwt;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import sample.clientapp.util.Base64url;
import sample.clientapp.util.JsonUtil;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class JsonWebToken {
    private String iss;
    private String sub;
    private String aud;

    private long exp;
    private long nbf;
    private long iat;
    private String jti;

    @JsonIgnore
    byte[] payload;
    @JsonIgnore
    String payloadString;
    @JsonIgnore
    byte[] signature;
    @JsonIgnore
    Header header;
    @JsonIgnore
    String tokenString;

    public static <T extends JsonWebToken> T parse(String str, Class<T> clazz) {
        if (str == null)
            return null;

        String[] parts = str.split("\\.");
        if (parts.length < 2 || parts.length > 3)
            return null;
        T jwt = JsonUtil.unmarshal(Base64url.decode(parts[1]), clazz);
        jwt.payload = Base64url.decode(parts[1]);
        jwt.header = JsonUtil.unmarshal(Base64url.decode(parts[0]), Header.class);
        jwt.signature = Base64url.decode(parts[2]);
        jwt.tokenString = str;
        return jwt;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getPayloadJSON() {
        Object obj = JsonUtil.unmarshal(this.payload, Object.class);
        return JsonUtil.marshal(obj);
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getTokenString() {
        return tokenString;
    }

    public void setTokenString(String tokenString) {
        this.tokenString = tokenString;
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Header {
        private String kid;
        private String alg;

        public String getKid() {
            return kid;
        }

        public void setKid(String kid) {
            this.kid = kid;
        }

        public String getAlg() {
            return alg;
        }

        public void setAlg(String alg) {
            this.alg = alg;
        }

    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public long getExp() {
        return exp;
    }

    public Date getExpDate() {
        return new Date(this.exp * 1000L);
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getNbf() {
        return nbf;
    }

    public void setNbf(long nbf) {
        this.nbf = nbf;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

}
