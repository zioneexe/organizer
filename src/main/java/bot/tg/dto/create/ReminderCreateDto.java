package bot.tg.dto.create;

import bot.tg.dto.DateTimeDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReminderCreateDto {

    private Long userId;

    private String text;

    private DateTimeDto dateTime;

    private LocalDateTime createdAt;

    public ReminderCreateDto(Long userId) {
        this.userId = userId;
        this.dateTime = new DateTimeDto();
        this.createdAt = LocalDateTime.now();
    }
}
