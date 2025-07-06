package bot.tg.command;

import bot.tg.provider.TelegramClientProvider;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.util.Constants.START_MESSAGE;
import static bot.tg.util.Constants.TASK_SELECTION;

public class StartCommand implements BotCommand {

    private final TelegramClient telegramClient;

    public StartCommand() {
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    @Override
    public void execute(Update update) {
        long chatId = update.getMessage().getChatId();

        SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text(START_MESSAGE)
                        .replyMarkup(ReplyKeyboardMarkup.builder()
                                .keyboardRow(new KeyboardRow(new KeyboardButton(TASK_SELECTION)))
                                .resizeKeyboard(true)
                                .oneTimeKeyboard(true)
                                .build())
                        .build();
        TelegramHelper.safeExecute(telegramClient, message);
    }
}
