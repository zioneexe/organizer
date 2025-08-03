package bot.tg.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TokenSerializationService {

    private final ObjectMapper objectMapper;

    public String serialize(TokenResponse tokenResponse) throws Exception {
        return objectMapper.writeValueAsString(tokenResponse);
    }

    public TokenResponse deserialize(String json) throws Exception {
        Map<String, Object> tokenMap = objectMapper.readValue(json, new TypeReference<>() {
        });

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken((String) tokenMap.get("access_token"));
        tokenResponse.setRefreshToken((String) tokenMap.get("refresh_token"));

        Number expiresIn = (Number) tokenMap.get("expires_in");
        tokenResponse.setExpiresInSeconds(expiresIn.longValue());

        return tokenResponse;
    }
}
