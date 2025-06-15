package bot.tg.state;

import bot.tg.dto.create.TaskCreateDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserStateManager {

    private final Map<Long, TaskCreateDto> drafts = new ConcurrentHashMap<>();

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    public void setState(long userId, UserState state) {
        userStates.put(userId, state);
    }

    public UserState getState(long userId) {
        return userStates.getOrDefault(userId, UserState.IDLE);
    }

    public void clearState(long userId) {
        userStates.remove(userId);
    }

    public TaskCreateDto getDraft(Long userId) {
        return drafts.get(userId);
    }

    public void createDraft(Long userId) {
        drafts.put(userId, new TaskCreateDto(userId));
    }
}
