package bot.tg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Time {

    public static final Time DEFAULT_REMINDER_TIME = new Time(7, 30, false);

    private int hour;

    private int minute;

    @BsonProperty("is_time_manually_edited")
    private boolean isTimeManuallyEdited = false;

    public Time(int hour, int minute) {
        this(hour, minute, false);
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d", hour, minute);
    }
}
