package bot.tg;

import bot.tg.server.JettyServer;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {

    public static final String API_KEY = System.getenv("TELEGRAM_BOT_API_KEY");

    public static void main(String[] args) throws Exception {
        JettyServer.start();
        OrganizerBot bot = new OrganizerBot(API_KEY);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🛑 Закриваємо підключення до Mongo...");
            bot.close();
        }));

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(API_KEY, bot);
            System.out.println("✅ Бот Organizer успішно стартанув!");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}