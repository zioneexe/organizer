package bot.tg.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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

        public static LocalDateTime toSystemLocalDateTime(DateTime dateTime) {
            ZonedDateTime zonedDateTime = toZonedDateTime(dateTime);
            return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }
    }
}
