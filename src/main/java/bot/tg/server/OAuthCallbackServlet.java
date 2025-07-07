package bot.tg.server;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.MongoTokenStore;
import bot.tg.repository.UserRepository;
import bot.tg.util.TelegramHelper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;

public class OAuthCallbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TelegramClient telegramClient = TelegramClientProvider.getInstance();
        UserRepository userRepository = RepositoryProvider.getUserRepository();
        MongoTokenStore tokenStore = RepositoryProvider.getTokenStore();

        String code = req.getParameter("code");
        String userIdString = req.getParameter("state");

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");

        if (code == null || userIdString == null) {
            resp.setStatus(400);
            resp.getWriter().write("Немає обов'язкових параметрів: коду або стану.");
            return;
        }

        try {
            Credential credential = GoogleOAuthService.exchangeCodeForTokens(code);
            TokenResponse tokenResponse = new TokenResponse()
                    .setAccessToken(credential.getAccessToken())
                    .setRefreshToken(credential.getRefreshToken())
                    .setExpiresInSeconds(credential.getExpiresInSeconds());

            String jsonTokens = CredentialSerializer.serialize(tokenResponse);
            tokenStore.store(userIdString, jsonTokens);

            long userId = Long.parseLong(userIdString);
            SendMessage message = SendMessage.builder()
                    .chatId(userId)
                    .text("✅ Ви успішно авторизувалися! Тепер ваші завдання та нагадування синхронізуватимуться з Google Calendar.")
                    .replyMarkup(new ReplyKeyboardRemove(true))
                    .build();
            TelegramHelper.safeExecute(telegramClient, message);
            userRepository.markAsGoogleConnected(userIdString, true);

            resp.getWriter().write("✅ Авторизація успішна! Можете повертатися до Telegram.");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("❌ Помилка: " + e.getMessage());
        }
    }
}
