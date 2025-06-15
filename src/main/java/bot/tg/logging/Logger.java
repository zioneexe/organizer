package bot.tg.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static void log(String firstName, String lastName, String userId, String text, String answer) {
        System.out.println("\n--------------------------------------------------");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        System.out.println("Message from: " + firstName + " " + lastName + ". (id = " + userId + ") \n Text - " + text);
        System.out.println("Bot Answer: \n Text - " + answer);
    }

    public static void log(String message) {
        System.out.println("\n--------------------------------------------------");
        System.out.println("message = " + message);
    }
}
