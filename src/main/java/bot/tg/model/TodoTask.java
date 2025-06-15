package bot.tg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoTask {

    @BsonId
    private ObjectId id;

    @BsonProperty("user_id")
    private Long userId;

    private String title;

    private String description;

    @BsonProperty("completed")
    private Boolean completed;

    @BsonIgnore
    private TaskStatus status;

    @BsonProperty("created_at")
    private LocalDateTime createdAt;

    @BsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @BsonCreator
    public TodoTask(@BsonProperty("completed") Boolean completed) {
        this.completed = completed;
        this.status = TaskStatus.fromBoolean(completed != null && completed);
    }

    public TaskStatus getStatus() {
        if (status == null && completed != null) {
            status = TaskStatus.fromBoolean(completed);
        }
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.completed = status != null && status.toBoolean();
    }

}