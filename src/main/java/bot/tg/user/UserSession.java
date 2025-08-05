package bot.tg.user;

import bot.tg.dto.Time;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.dto.create.TaskCreateDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserSession {
    private Long userId;

    @Builder.Default
    private UserState state = UserState.IDLE;

    @Builder.Default
    private int currentTaskPage = 1;

    @Builder.Default
    private int currentReminderPage = 1;

    @Builder.Default
    private TaskCreateDto taskDraft = null;

    @Builder.Default
    private ReminderCreateDto reminderDraft = null;

    @Builder.Default
    private String editingTaskId = null;

    @Builder.Default
    private Time morningGreetingTimeDraft = Time.DEFAULT_REMINDER_TIME;

    public void clearEditingTaskId() {
        this.editingTaskId = null;
    }

    public void clearReminderDraft() {
        this.reminderDraft = null;
    }

    public void clearTaskDraft() {
        this.taskDraft = null;
    }

    public void setIdleState() {
        this.state = UserState.IDLE;
    }

    public void createTaskDraft() {
        this.taskDraft = new TaskCreateDto(userId);
    }

    public void createReminderDraft() {
        this.reminderDraft = new ReminderCreateDto(userId);
    }
}
