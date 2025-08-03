package bot.tg.service;

import bot.tg.dto.Pageable;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Core.DEFAULT_PAGE_SIZE;

@Service
@RequiredArgsConstructor
public class PaginationService {

    private final TaskRepository taskRepository;
    private final ReminderRepository reminderRepository;

    public Pageable formTaskPageableForUser(int wantedPage, long userId, LocalDate date, ZoneId userZoneId) {
        long tasksByUserForDay = taskRepository.countByUserForDay(userId, date, userZoneId);

        int pageSize = DEFAULT_PAGE_SIZE;
        int totalPages = (int) Math.ceil(tasksByUserForDay * 1.0 / pageSize);
        if (wantedPage > totalPages) wantedPage = totalPages;

        return Pageable.of(wantedPage, pageSize, totalPages);
    }

    public Pageable formReminderPageableForUser(int wantedPage, long userId, ZoneId userZoneId) {
        long tasksByUserForDay = reminderRepository.countUpcomingByUser(userId, userZoneId);

        int pageSize = DEFAULT_PAGE_SIZE;
        int totalPages = (int) Math.ceil(tasksByUserForDay * 1.0 / pageSize);
        if (wantedPage > totalPages) wantedPage = totalPages;

        return Pageable.of(wantedPage, pageSize, totalPages);
    }
}
