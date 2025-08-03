package bot.tg.util;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public class TelegramUserExtractor {
    private TelegramUserExtractor() {
    }

    public static Long getUserId(Update update) {
        User user = extractUser(update);
        if (user == null) {
            throw new IllegalArgumentException("User not found in update: " + update);
        }

        return user.getId();
    }

    public static String getFirstName(Update update) {
        User user = extractUser(update);
        if (user == null) {
            throw new IllegalArgumentException("User not found in update: " + update);
        }

        return user.getFirstName();
    }

    public static String getLastName(Update update) {
        User user = extractUser(update);
        if (user == null) {
            throw new IllegalArgumentException("User not found in update: " + update);
        }

        return user.getLastName();
    }

    public static String getUsername(Update update) {
        User user = extractUser(update);
        if (user == null) {
            throw new IllegalArgumentException("User not found in update: " + update);
        }

        return user.getUserName();
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
