package bot.tg.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.TokenResponse;

import java.util.Map;

public class CredentialSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serialize(TokenResponse tokenResponse) throws Exception {
        return mapper.writeValueAsString(tokenResponse);
    }

    public static TokenResponse deserialize(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> tokenMap = mapper.readValue(json, new TypeReference<>(){});

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken((String) tokenMap.get("access_token"));
        tokenResponse.setRefreshToken((String) tokenMap.get("refresh_token"));

        Number expiresIn = (Number) tokenMap.get("expires_in");
        tokenResponse.setExpiresInSeconds(expiresIn.longValue());

        return tokenResponse;
    }
}
