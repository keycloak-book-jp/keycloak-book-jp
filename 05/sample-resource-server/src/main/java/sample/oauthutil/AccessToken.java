package sample.oauthutil;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccessToken {
    private long exp;
    private String scope;
    private String preferredUsername;

    public long getExp() {
        return exp;
    }

    public void setExp(long value) {
        exp = value;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String value) {
        scope = value;
    }

    public List<String> getScopeList() {
        return Arrays.asList(scope.split(" "));
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String value) {
        preferredUsername = value;
    }
}
