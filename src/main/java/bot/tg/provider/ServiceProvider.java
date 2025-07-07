package bot.tg.provider;

import bot.tg.callback.CallbackDispatcher;
import bot.tg.service.MessageService;
import bot.tg.state.StateDispatcher;
import bot.tg.state.UserStateManager;
import lombok.Getter;

public class ServiceProvider {

    @Getter
    private static UserStateManager userStateManager;
    @Getter
    private static StateDispatcher stateDispatcher;
    @Getter
    private static CallbackDispatcher callbackDispatcher;
    @Getter
    private static MessageService messageService;

    public static void init() {
        ServiceProvider.messageService = new MessageService();
        ServiceProvider.userStateManager = new UserStateManager();
        ServiceProvider.stateDispatcher = new StateDispatcher();
        ServiceProvider.callbackDispatcher = new CallbackDispatcher();
    }
}
