package bot.tg.dto.update;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReminderUpdateDto {

    private LocalDateTime time;
}
