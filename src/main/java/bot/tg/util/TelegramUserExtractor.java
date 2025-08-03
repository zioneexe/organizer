package bot.tg.util;

import bot.tg.dto.TelegramUser;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public class TelegramUserExtractor {
    private TelegramUserExtractor() {
    }

    public static TelegramUser extractTelegramUser(Update update) {
        User user = getUserIfPossible(update);
        return new TelegramUser(
                user.getId(),
                user.getUserName(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public static Long getUserId(Update update) {
        return getUserIfPossible(update).getId();
    }

    public static String getFirstName(Update update) {
        return getUserIfPossible(update).getFirstName();
    }

    public static String getLastName(Update update) {
        return getUserIfPossible(update).getLastName();
    }

    public static String getUsername(Update update) {
        return getUserIfPossible(update).getUserName();
    }

    private static User getUserIfPossible(Update update) {
        User user = extractUser(update);
        if (user == null) {
            throw new IllegalArgumentException("User not found in update: " + update);
        }
        return user;
    }

    private static User extractUser(Update update) {
        if (update.hasMessage()) return update.getMessage().getFrom();
        if (update.hasCallbackQuery()) return update.getCallbackQuery().getFrom();
        if (update.hasInlineQuery()) return update.getInlineQuery().getFrom();
        if (update.hasMyChatMember()) return update.getMyChatMember().getFrom();
        if (update.hasChatJoinRequest()) return update.getChatJoinRequest().getUser();
        return null;
    }
}
