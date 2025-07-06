package bot.tg.mapper;

import bot.tg.dto.DateTimeDto;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.model.Reminder;

import java.time.LocalDateTime;

public class ReminderMapper {

    public static Reminder fromDto(ReminderCreateDto dto) {
        DateTimeDto dateTimeDto = dto.getDateTime();
        LocalDateTime dateTime = dateTimeDto.getDate().atTime(dateTimeDto.getHour(), dateTimeDto.getMinute());

        return new Reminder(
                null,
                dto.getUserId(),
                dto.getText(),
                dateTime,
                false,
                dto.getCreatedAt(),
                null
        );
    }
}
