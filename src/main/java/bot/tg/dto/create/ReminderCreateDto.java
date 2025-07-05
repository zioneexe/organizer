package bot.tg.dto.create;

import bot.tg.dto.DateTimeDto;
import lombok.Data;

@Data
public class ReminderCreateDto {

    private DateTimeDto time;

    private String Text;
}
