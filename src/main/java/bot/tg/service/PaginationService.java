package bot.tg.service;

import bot.tg.dto.Pageable;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Core.DEFAULT_PAGE_SIZE;

@Slf4j
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

        log.debug("Forming task pageable: userId={}, date={}, tasks={}, pageSize={}, totalPages={}, wantedPage={}",
                userId, date, tasksByUserForDay, pageSize, totalPages, wantedPage);

        return Pageable.of(wantedPage, pageSize, totalPages);
    }

    public Pageable formReminderPageableForUser(int wantedPage, long userId, ZoneId userZoneId) {
        long remindersByUser = reminderRepository.countUpcomingByUser(userId, userZoneId);
        int pageSize = DEFAULT_PAGE_SIZE;
        int totalPages = (int) Math.ceil(remindersByUser * 1.0 / pageSize);
        if (wantedPage > totalPages) wantedPage = totalPages;

        log.debug("Forming reminder pageable: userId={}, upcomingReminders={}, pageSize={}, totalPages={}, wantedPage={}",
                userId, remindersByUser, pageSize, totalPages, wantedPage);

        return Pageable.of(wantedPage, pageSize, totalPages);
    }
}
