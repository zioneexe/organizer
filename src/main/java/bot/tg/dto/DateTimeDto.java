package bot.tg.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
public class DateTimeDto {

    private LocalDate date;

    private int hour;

    private int minute;

    private String timeZone;

    public static class DateTimeMapper {

        public static ZonedDateTime toZonedDateTime(DateTimeDto dto) {
            LocalDateTime localDateTime = dto.getDate()
                    .atTime(dto.getHour(), dto.getMinute());

            return localDateTime.atZone(ZoneId.of(dto.getTimeZone()));
        }

        public static LocalDateTime toSystemLocalDateTime(DateTimeDto dto) {
            ZonedDateTime zonedDateTime = toZonedDateTime(dto);
            return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }
    }
}
