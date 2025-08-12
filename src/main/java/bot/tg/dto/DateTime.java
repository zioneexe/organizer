package bot.tg.dto;

import lombok.Data;

import java.time.*;

@Data
public class DateTime {

    private boolean isTimeManuallyEdited = false;

    private LocalDate date;

    private int hour;

    private int minute;

    private String timeZone;

    public static class DateTimeMapper {

        public static ZonedDateTime toZonedDateTime(DateTime dateTime) {
            LocalDateTime localDateTime = dateTime.getDate()
                    .atTime(dateTime.getHour(), dateTime.getMinute());

            return localDateTime.atZone(ZoneId.of(dateTime.getTimeZone()));
        }

        public static LocalDateTime toUTCLocalDateTime(DateTime dateTime) {
            ZonedDateTime zonedDateTime = toZonedDateTime(dateTime);
            return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        }
    }
}
