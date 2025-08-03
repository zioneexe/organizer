package bot.tg.state;

import bot.tg.dto.Time;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.dto.create.TaskCreateDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStateManager {

    private final Map<Long, TaskCreateDto> taskDrafts = new ConcurrentHashMap<>();
    private final Map<Long, ReminderCreateDto> reminderDrafts = new ConcurrentHashMap<>();
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, Integer> userTaskPages = new ConcurrentHashMap<>();
    private final Map<Long, Integer> userReminderPages = new ConcurrentHashMap<>();
    private final Map<Long, String> userEditingTasks = new ConcurrentHashMap<>();
    private final Map<Long, Time> userMorningGreetingTimeDrafts = new ConcurrentHashMap<>();

    public void setCurrentReminderPage(Long userId, Integer page) {
        userReminderPages.put(userId, page);
    }

    public int getCurrentReminderPage(Long userId) {
        return userReminderPages.getOrDefault(userId, 1);
    }

    public void setCurrentTaskPage(Long userId, Integer page) {
        userTaskPages.put(userId, page);
    }

    public int getCurrentTaskPage(Long userId) {
        return userTaskPages.getOrDefault(userId, 1);
    }

    public void setState(Long userId, UserState state) {
        userStates.put(userId, state);
    }

    public UserState getState(Long userId) {
        return userStates.getOrDefault(userId, UserState.IDLE);
    }

    public TaskCreateDto getTaskDraft(Long userId) { return taskDrafts.get(userId); }

    public void createTaskDraft(Long userId) { taskDrafts.put(userId, new TaskCreateDto(userId)); }

    public ReminderCreateDto getReminderDraft(Long userId) {
        return reminderDrafts.get(userId);
    }

    public void createReminderDraft(Long userId) { reminderDrafts.put(userId, new ReminderCreateDto(userId)); }

    public void setEditingTaskId(Long userId, String taskId) {
        userEditingTasks.put(userId, taskId);
    }

    public String getEditingTaskId(Long userId) {
        return userEditingTasks.get(userId);
    }

    public void clearEditingTaskId(Long userId) {
        userEditingTasks.remove(userId);
    }

    public void setMorningGreetingTimeDraft(Long userId, Time time) {
        userMorningGreetingTimeDrafts.put(userId, time);
    }

    public Time getMorningGreetingTimeDraft(Long userId) {
        return userMorningGreetingTimeDrafts.get(userId);
    }

}
