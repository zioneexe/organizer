package bot.tg.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DateTimeDto {

    private LocalDate date;

    private int hour;

    private int minute;
}
