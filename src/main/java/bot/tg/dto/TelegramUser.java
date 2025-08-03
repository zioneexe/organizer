package bot.tg.dto;

public record TelegramUser(long userId, String username, String firstName, String lastName) {
}