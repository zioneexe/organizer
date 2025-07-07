package bot.tg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.TokenResponse;

public class CredentialSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serialize(TokenResponse tokenResponse) throws Exception {
        return mapper.writeValueAsString(tokenResponse);
    }

    public static TokenResponse deserialize(String json) throws Exception {
        return mapper.readValue(json, TokenResponse.class);
    }
}
