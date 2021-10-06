package sample.clientapp.jwt;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccessToken extends JsonWebToken {
    private String scope;

    public String getScope() {
        return scope;
    }

    public void setScope(String value) {
        scope = value;
    }

    public List<String> getScopeList() {
        return Arrays.asList(scope.split(" "));
    }
}
