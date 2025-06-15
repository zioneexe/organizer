package bot.tg.dto.create;

import bot.tg.model.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCreateDto {

    private Long userId;

    private String title;

    private String description;

    private TaskStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public TaskCreateDto(Long userId) {
        this.userId = userId;
        this.status = TaskStatus.IN_PROGRESS;
        this.createdAt = LocalDateTime.now();
    }
}
