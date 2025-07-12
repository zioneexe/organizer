package bot.tg.util;

import bot.tg.dto.Pageable;
import bot.tg.provider.RepositoryProvider;
import bot.tg.repository.TaskRepository;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Core.DEFAULT_PAGE_SIZE;

public class PaginationHelper {

    public static Pageable formPageableForUser(int wantedPage, long userId, LocalDate date, ZoneId userZoneId) {
        TaskRepository taskRepository = RepositoryProvider.getTaskRepository();
        long tasksByUserForDay = taskRepository.countByUserForDay(userId, date, userZoneId);
        int pageSize = DEFAULT_PAGE_SIZE;
        int totalPages = (int) Math.ceil(tasksByUserForDay * 1.0 / pageSize);

        return Pageable.of(wantedPage, pageSize, totalPages);
    }
}
