package bot.tg.provider;

import bot.tg.callback.CallbackDispatcher;
import bot.tg.state.StateDispatcher;
import bot.tg.state.UserStateManager;

public class ServiceProvider {
    private static final ServiceProvider INSTANCE = new ServiceProvider();

    private static UserStateManager userStateManager;
    private static StateDispatcher stateDispatcher;
    private static CallbackDispatcher callbackDispatcher;

    public static void init() {
        ServiceProvider.userStateManager = new UserStateManager();
        ServiceProvider.stateDispatcher = new StateDispatcher();
        ServiceProvider.callbackDispatcher = new CallbackDispatcher();
    }

    public static ServiceProvider getInstance() {
        return INSTANCE;
    }

    public UserStateManager getUserStateManager() {
        return userStateManager;
    }

    public StateDispatcher getStateDispatcher() {
        return stateDispatcher;
    }

    public CallbackDispatcher getCallbackDispatcher() {
        return callbackDispatcher;
    }
}
