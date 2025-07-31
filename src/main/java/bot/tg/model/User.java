package bot.tg.model;

import bot.tg.dto.Time;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @BsonProperty("user_id")
    private Long userId;

    @BsonProperty("first_name")
    private String firstName;

    @BsonProperty("last_name")
    private String lastName;

    private String username;

    @BsonProperty("time_zone")
    private String timeZone;

    @BsonProperty("is_google_connected")
    private Boolean isGoogleConnected;

    @BsonProperty("greetings_enabled")
    private Boolean greetingsEnabled;

    @BsonProperty("preferred_greeting_time")
    private Time preferredGreetingTime;

}
