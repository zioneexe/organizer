package bot.tg.util;

import bot.tg.provider.TelegramClientProvider;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.stickers.GetStickerSet;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.api.objects.stickers.StickerSet;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Random;

public class StickerHelper {

    public static SendSticker sendSticker(Update update) {
        TelegramClient telegramClient = TelegramClientProvider.getInstance();

        if (update.hasMessage() && update.getMessage().hasSticker()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();
            Sticker receivedSticker = message.getSticker();
            String stickerSetName = receivedSticker.getSetName();

            if (stickerSetName == null) return null;

            GetStickerSet getStickerSet = new GetStickerSet(stickerSetName);

            try {
                StickerSet stickerSet = telegramClient.execute(getStickerSet);
                List<Sticker> stickers = stickerSet.getStickers();

                List<Sticker> otherStickers = stickers.stream()
                        .filter(sticker -> !sticker.getFileId().equals(receivedSticker.getFileId()))
                        .toList();

                if (!otherStickers.isEmpty()) {
                    Sticker randomSticker = otherStickers.get(new Random().nextInt(otherStickers.size()));

                    return SendSticker.builder()
                            .chatId(chatId)
                            .sticker(new InputFile(randomSticker.getFileId()))
                            .build();
                }

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
