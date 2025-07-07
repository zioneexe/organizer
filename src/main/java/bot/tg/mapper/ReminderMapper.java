package bot.tg.mapper;

import bot.tg.dto.DateTimeDto;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.model.Reminder;

import java.time.LocalDateTime;

public class ReminderMapper {

    public static Reminder fromDto(ReminderCreateDto dto) {
        LocalDateTime systemDateTime = DateTimeDto.DateTimeMapper.toSystemLocalDateTime(dto.getDateTime());

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
