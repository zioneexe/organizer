package bot.tg.provider;

import bot.tg.callback.CallbackDispatcher;
import bot.tg.command.CommandRegistry;
import bot.tg.service.GoogleCalendarService;
import bot.tg.service.MessageService;
import bot.tg.state.StateDispatcher;
import bot.tg.state.UserStateManager;
import lombok.Getter;

public class ServiceProvider {

    @Getter
    private static MessageService messageService;
    @Getter
    private static UserStateManager userStateManager;
    @Getter
    private static CommandRegistry commandRegistry;
    @Getter
    private static StateDispatcher stateDispatcher;
    @Getter
    private static CallbackDispatcher callbackDispatcher;
    @Getter
    private static GoogleCalendarService googleCalendarService;

    public static void init() {
        // needs to be instantiated before handlers
        // because they are dependent on it
        ServiceProvider.messageService = new MessageService();
        ServiceProvider.googleCalendarService = new GoogleCalendarService();

        ServiceProvider.userStateManager = new UserStateManager();
        ServiceProvider.commandRegistry = new CommandRegistry();
        ServiceProvider.stateDispatcher = new StateDispatcher();
        ServiceProvider.callbackDispatcher = new CallbackDispatcher();
    }
}
