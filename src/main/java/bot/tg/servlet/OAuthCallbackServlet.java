package bot.tg.servlet;

import bot.tg.helper.TelegramHelper;
import bot.tg.repository.MongoTokenStore;
import bot.tg.repository.UserRepository;
import bot.tg.service.GoogleClientService;
import bot.tg.service.TokenSerializationService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;

import static bot.tg.constant.ResponseMessage.ERROR;

@Component
@RequiredArgsConstructor
public class OAuthCallbackServlet extends HttpServlet {

    private static final String MISSING_PARAMS = "Немає обов'язкових параметрів: коду або стану.";
    private static final String GOOGLE_CONNECT_SUCCESS = "✅ Ви успішно авторизувалися! Тепер ваші завдання та нагадування синхронізуватимуться з Google Calendar.";
    private static final String GOOGLE_CONNECT_SUCCESS_REDIRECT = "✅ Авторизація успішна! Можете повертатися до Telegram.";

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final MongoTokenStore tokenStore;
    private final GoogleClientService googleClientService;
    private final TokenSerializationService tokenSerializationService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getParameter("code");
        String userIdString = req.getParameter("state");

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");

        if (code == null || userIdString == null) {
            resp.setStatus(400);
            resp.getWriter().write(MISSING_PARAMS);
            return;
        }

        try {
            Credential credential = googleClientService.exchangeCodeForTokens(code);
            TokenResponse tokenResponse = new TokenResponse()
                    .setAccessToken(credential.getAccessToken())
                    .setRefreshToken(credential.getRefreshToken())
                    .setExpiresInSeconds(credential.getExpiresInSeconds());

            String jsonTokens = tokenSerializationService.serialize(tokenResponse);
            tokenStore.store(userIdString, jsonTokens);

            Long userId = Long.parseLong(userIdString);
            TelegramHelper.sendMessageWithKeyboardRemove(
                    telegramClient,
                    userId,
                    GOOGLE_CONNECT_SUCCESS
            );
            userRepository.setGoogleConnected(userId, true);

            resp.getWriter().write(GOOGLE_CONNECT_SUCCESS_REDIRECT);
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(ERROR + e.getMessage());
        }
    }
}
