package bot.tg.mapper;

import bot.tg.dto.create.TaskCreateDto;
import bot.tg.model.TodoTask;

public class TaskMapper {

    public static TodoTask fromDto(TaskCreateDto dto) {
        return new TodoTask(
                null,
                dto.getUserId(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getStatus() != null && dto.getStatus().toBoolean(),
                dto.getStatus(),
                dto.getCreatedAt(),
                null
        );
    }
}
