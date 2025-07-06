package bot.tg.mapper;

import bot.tg.dto.DateTimeDto;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.model.Reminder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ReminderMapper {

    public static Reminder fromDto(ReminderCreateDto dto) {
        DateTimeDto dateTimeDto = dto.getDateTime();

        LocalDateTime localDateTime = dateTimeDto.getDate()
                .atTime(dateTimeDto.getHour(), dateTimeDto.getMinute());
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(dateTimeDto.getTimeZone()));

        LocalDateTime systemDateTime = zonedDateTime
                .withZoneSameInstant(ZoneOffset.systemDefault())
                .toLocalDateTime();

        return new Reminder(
                null,
                dto.getUserId(),
                dto.getText(),
                systemDateTime,
                false,
                dto.getCreatedAt(),
                null
        );
    }
}
