package bot.tg.provider;

import bot.tg.callback.CallbackDispatcher;
import bot.tg.command.CommandRegistry;
import bot.tg.service.MessageService;
import bot.tg.state.StateDispatcher;
import bot.tg.state.UserStateManager;
import lombok.Getter;

public class ServiceProvider {

    @Getter
    private static UserStateManager userStateManager;
    @Getter
    private static CommandRegistry commandRegistry;
    @Getter
    private static StateDispatcher stateDispatcher;
    @Getter
    private static CallbackDispatcher callbackDispatcher;
    @Getter
    private static MessageService messageService;

    public static void init() {
        ServiceProvider.userStateManager = new UserStateManager();
        ServiceProvider.commandRegistry = new CommandRegistry();
        ServiceProvider.stateDispatcher = new StateDispatcher();
        ServiceProvider.callbackDispatcher = new CallbackDispatcher();
        ServiceProvider.messageService = new MessageService();
    }
}
