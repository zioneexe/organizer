package bot.tg.dto;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.location.Location;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;

public class TelegramContext {
    public final long userId;
    public final long chatId;
    public final int messageId;
    public final String text;

    public final String data;
    public final String callbackQueryId;

    public final Location location;
    public final Sticker sticker;

    public TelegramContext(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            this.userId = message.getFrom().getId();
            this.chatId = message.getChatId();
            this.messageId = message.getMessageId();
            this.text = message.getText();
            this.data = null;
            this.callbackQueryId = null;
            this.location = message.getLocation();
            this.sticker = message.getSticker();
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            MaybeInaccessibleMessage message = query.getMessage();
            this.userId = query.getFrom().getId();
            this.chatId = message.getChatId();
            this.messageId = message.getMessageId();
            this.text = null;
            this.data = query.getData();
            this.callbackQueryId = query.getId();
            this.location = null;
            this.sticker = null;
        } else {
            throw new IllegalArgumentException("Тип оновлення не підтримується: " + update);
        }
    }
}
