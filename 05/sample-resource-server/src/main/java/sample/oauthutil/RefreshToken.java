package sample.oauthutil;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RefreshToken {
    private long exp;

    public long getExp() {
        return exp;
    }

    public void setExp(long value) {
        exp = value;
    }
}
