package bot.tg.command;

import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static bot.tg.util.Constants.*;

public class NewReminderCommand implements BotCommand {

    private final UserStateManager userStateManager;
    private final TelegramClient telegramClient;

    public NewReminderCommand() {
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    @Override
    public void execute(Update update) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();

        SendMessage removeKeyboard = SendMessage.builder()
                .chatId(chatId)
                .text("Окей!")
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
        TelegramHelper.safeExecute(telegramClient, removeKeyboard);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(REMINDER_DATE)
                .replyMarkup(buildDateChoiceKeyboard())
                .build();

        userStateManager.createReminderDraft(userId);
        userStateManager.setState(userId, UserState.AWAITING_REMINDER_DATE);
        TelegramHelper.safeExecute(telegramClient, sendMessage);
    }

    private InlineKeyboardMarkup buildDateChoiceKeyboard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            String text = switch (i) {
                case 0 -> "Сьогодні";
                case 1 -> "Завтра";
                default -> date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("uk")));
            };
            String callbackData = DATE_REMINDER + COLON_DELIMITER + date;

            InlineKeyboardButton btn = InlineKeyboardButton.builder()
                    .text(text)
                    .callbackData(callbackData)
                    .build();

            rows.add(new InlineKeyboardRow(btn));
        }

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

}
