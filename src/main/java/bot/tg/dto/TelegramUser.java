package bot.tg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class TelegramUser {

    private Long userId;

    private String username;

    private String firstName;

    private String lastName;
}