package bot.tg.dto.create;

import bot.tg.dto.DateTime;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class ReminderCreateDto {

    private Long userId;

    private String text;

    private DateTime dateTime;

    private LocalDateTime createdAt;

    public ReminderCreateDto(Long userId) {
        this.userId = userId;
        this.dateTime = new DateTime();
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
