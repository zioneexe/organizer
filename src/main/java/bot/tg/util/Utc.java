package bot.tg.util;

import java.time.Instant;
import java.util.Date;

public class Utc {
    public static Date now() {
        return Date.from(Instant.now());
    }
}
