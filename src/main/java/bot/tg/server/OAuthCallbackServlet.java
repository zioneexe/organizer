package bot.tg.server;

import bot.tg.provider.RepositoryProvider;
import bot.tg.repository.MongoTokenStore;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class OAuthCallbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        MongoTokenStore tokenStore = RepositoryProvider.getTokenStore();

        String code = req.getParameter("code");
        String state = req.getParameter("state");

        if (code == null || state == null) {
            resp.setStatus(400);
            resp.getWriter().write("Missing code or state");
            return;
        }

        try {
            Credential credential = GoogleOAuthService.exchangeCodeForTokens(code);
            TokenResponse tokenResponse = new TokenResponse()
                    .setAccessToken(credential.getAccessToken())
                    .setRefreshToken(credential.getRefreshToken())
                    .setExpiresInSeconds(credential.getExpiresInSeconds());

            String jsonTokens = CredentialSerializer.serialize(tokenResponse);
            tokenStore.store(state, jsonTokens);

            resp.getWriter().write("✅ Авторизація успішна! Можете повертатися до Telegram.");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("❌ Помилка: " + e.getMessage());
        }
    }
}
