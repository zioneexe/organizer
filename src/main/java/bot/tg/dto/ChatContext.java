package bot.tg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatContext {

    private long userId;
    private long chatId;
}
