package bot.tg.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.oauth2.TokenStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleClientService {

    private static final String SERVER_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String REVOKE_URL = "https://oauth2.googleapis.com/revoke";

    private static final String UNAUTHORIZED_USER = "❌ Користувач не авторизований";
    private static final String APPLICATION_NAME = "Organizer";

    private static final JsonFactory JSON_FACTORY = new GsonFactory();
    private static final NetHttpTransport HTTP_TRANSPORT = createHttpTransport();

    private final TokenStore tokenStore;
    private final TokenSerializationService tokenSerializationService;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;
    @Value("${GOOGLE_CLIENT_ID}")
    public String clientId;
    @Value("${GOOGLE_REDIRECT_URI}")
    public String redirectUri;

    public String getAuthorizationUrl(String telegramUserId) {
        String url = new GoogleAuthorizationCodeRequestUrl(clientId, redirectUri, List.of(CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_EVENTS))
                .setAccessType("offline")
                .set("prompt", "consent")
                .setState(telegramUserId)
                .build();
        log.debug("Generated Google authorization URL for userId={}: {}", telegramUserId, url);
        return url;
    }

    public Credential getCredentialFromStoredTokens(String userId) throws Exception {
        String json = tokenStore.load(userId);
        if (json == null) {
            log.warn("No stored token found for userId={}", userId);
            throw new IllegalStateException(UNAUTHORIZED_USER);
        }

        TokenResponse tokenResponse = tokenSerializationService.deserialize(json);
        log.debug("Loaded credential from stored token for userId={}", userId);

        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
                .setTokenServerEncodedUrl(SERVER_TOKEN_URL)
                .build()
                .setFromTokenResponse(tokenResponse);
    }

    public Calendar getCalendarService(Credential credential) {
        log.debug("Creating Calendar service instance");
        return new Calendar.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                credential
        ).setApplicationName(APPLICATION_NAME).build();
    }

    public Credential exchangeCodeForTokens(String code) throws Exception {
        log.debug("Exchanging authorization code for tokens");
        var tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientId,
                clientSecret,
                code,
                redirectUri)
                .execute();

        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
                .setTokenServerEncodedUrl(SERVER_TOKEN_URL)
                .build()
                .setFromTokenResponse(tokenResponse);
    }

    public void revokeRefreshTokenForUser(String userId) throws Exception {
        String tokenString = tokenStore.load(userId);
        TokenResponse tokenResponse = tokenSerializationService.deserialize(tokenString);

        log.info("Revoking refresh token for userId={}", userId);
        String refreshToken = tokenResponse.getRefreshToken();
        revokeToken(refreshToken);
    }

    private void revokeToken(String token) throws IOException {
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();

        GenericUrl url = new GenericUrl(REVOKE_URL);
        UrlEncodedContent content = new UrlEncodedContent(Collections.singletonMap("token", token));

        HttpRequest request = requestFactory.buildPostRequest(url, content);
        HttpResponse response = request.execute();

        log.debug("Token revoked, response code={}", response.getStatusCode());
        response.disconnect();
    }

    private static NetHttpTransport createHttpTransport() {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
