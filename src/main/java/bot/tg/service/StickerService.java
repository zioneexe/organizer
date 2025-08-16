package bot.tg.service;

import bot.tg.dto.TelegramContext;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.stickers.GetStickerSet;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.api.objects.stickers.StickerSet;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class StickerService {

    private final TelegramClient telegramClient;

    public SendSticker sendSticker(UserRequest request) {
        TelegramContext context = request.getContext();

        if (context.sticker == null) {
            log.debug("No sticker in context for userId={}", context.userId);
            return null;
        }

        String stickerSetName = context.sticker.getSetName();

        if (stickerSetName == null) {
            log.debug("Sticker in context has no set name for userId={}", context.userId);
            return null;
        }

        GetStickerSet getStickerSet = new GetStickerSet(stickerSetName);

        try {
            StickerSet stickerSet = telegramClient.execute(getStickerSet);
            List<Sticker> stickers = stickerSet.getStickers();
            log.debug("Retrieved {} stickers from set '{}' for userId={}", stickers.size(), stickerSetName, context.userId);

            List<Sticker> otherStickers = stickers.stream()
                    .filter(sticker -> !sticker.getFileId().equals(context.sticker.getFileId()))
                    .toList();

            if (!otherStickers.isEmpty()) {
                Sticker randomSticker = otherStickers.get(new Random().nextInt(otherStickers.size()));

                return SendSticker.builder()
                        .chatId(context.userId)
                        .sticker(new InputFile(randomSticker.getFileId()))
                        .build();
            } else {
                log.debug("No alternative stickers found in set '{}' for userId={}", stickerSetName, context.userId);
            }
        } catch (TelegramApiException e) {
            log.error("Failed to retrieve stickers from set '{}': {}", stickerSetName, e.getMessage(), e);
        }

        return null;
    }
}
