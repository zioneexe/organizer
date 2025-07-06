package bot.tg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {

    @BsonId
    private ObjectId id;

    @BsonProperty("user_id")
    private Long userId;

    private String text;

    @BsonProperty("date_time")
    private LocalDateTime dateTime;

    private Boolean fired;

    @BsonProperty("created_at")
    private LocalDateTime createdAt;

    @BsonProperty("updated_at")
    private LocalDateTime updatedAt;

}
