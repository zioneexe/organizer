package bot.tg.server;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import java.util.Collections;
import java.util.List;

public class GoogleOAuthService {

    public static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
    public static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");
    public static final String REDIRECT_URI = System.getenv("GOOGLE_REDIRECT_URI");

    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/calendar.events");

    private static final JsonFactory JSON_FACTORY = new GsonFactory();
    private static final NetHttpTransport HTTP_TRANSPORT = createHttpTransport();

    private static NetHttpTransport createHttpTransport() {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAuthorizationUrl(String telegramUserId) {
        return new GoogleAuthorizationCodeRequestUrl(CLIENT_ID, REDIRECT_URI, SCOPES)
                .setAccessType("offline")
                .set("prompt", "consent")
                .setState(telegramUserId)
                .build();
    }

    public static Credential exchangeCodeForTokens(String code) throws Exception {
        var tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                CLIENT_ID,
                CLIENT_SECRET,
                code,
                REDIRECT_URI)
                .execute();

        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientAuthentication(new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET))
                .setTokenServerEncodedUrl("https://oauth2.googleapis.com/token")
                .build()
                .setFromTokenResponse(tokenResponse);
    }
}
