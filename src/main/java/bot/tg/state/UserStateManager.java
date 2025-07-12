package bot.tg.state;

import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.dto.create.TaskCreateDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserStateManager {

    private final Map<Long, TaskCreateDto> taskDrafts = new ConcurrentHashMap<>();
    private final Map<Long, ReminderCreateDto> reminderDrafts = new ConcurrentHashMap<>();
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, Integer> userTaskPages = new ConcurrentHashMap<>();

    public void setCurrentTaskPage(long userId, int page) {
        userTaskPages.put(userId, page);
    }

    public int getCurrentTaskPage(long userId) {
        return userTaskPages.getOrDefault(userId, 1);
    }

    public void setState(long userId, UserState state) {
        userStates.put(userId, state);
    }

    public UserState getState(long userId) {
        return userStates.getOrDefault(userId, UserState.IDLE);
    }

    public void clearTaskDraft(long userId) { taskDrafts.remove(userId); }

    public TaskCreateDto getTaskDraft(Long userId) { return taskDrafts.get(userId); }

    public void createTaskDraft(Long userId) { taskDrafts.put(userId, new TaskCreateDto(userId)); }

    public void clearReminderDraft(long userId) { reminderDrafts.remove(userId); }

    public ReminderCreateDto getReminderDraft(Long userId) {
        return reminderDrafts.get(userId);
    }

    public void createReminderDraft(Long userId) { reminderDrafts.put(userId, new ReminderCreateDto(userId)); }
}
