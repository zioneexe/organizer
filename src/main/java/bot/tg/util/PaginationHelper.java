package bot.tg.util;

import bot.tg.dto.Pageable;
import bot.tg.provider.RepositoryProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.TaskRepository;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Core.DEFAULT_PAGE_SIZE;

public class PaginationHelper {

    public static Pageable formTaskPageableForUser(int wantedPage, long userId, LocalDate date, ZoneId userZoneId) {
        TaskRepository taskRepository = RepositoryProvider.getTaskRepository();
        long tasksByUserForDay = taskRepository.countByUserForDay(userId, date, userZoneId);

        int pageSize = DEFAULT_PAGE_SIZE;
        int totalPages = (int) Math.ceil(tasksByUserForDay * 1.0 / pageSize);
        if (wantedPage > totalPages) wantedPage = totalPages;

        return Pageable.of(wantedPage, pageSize, totalPages);
    }

    public static Pageable formReminderPageableForUser(int wantedPage, long userId, ZoneId userZoneId) {
        ReminderRepository reminderRepository = RepositoryProvider.getReminderRepository();
        long tasksByUserForDay = reminderRepository.countUpcomingByUser(userId, userZoneId);

        int pageSize = DEFAULT_PAGE_SIZE;
        int totalPages = (int) Math.ceil(tasksByUserForDay * 1.0 / pageSize);
        if (wantedPage > totalPages) wantedPage = totalPages;

        return Pageable.of(wantedPage, pageSize, totalPages);
    }
}
