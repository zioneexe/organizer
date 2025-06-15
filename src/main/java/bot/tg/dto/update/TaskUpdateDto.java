package bot.tg.dto.update;

import bot.tg.model.TaskStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskUpdateDto {

    private String title;

    private String description;

    private TaskStatus status;
}
