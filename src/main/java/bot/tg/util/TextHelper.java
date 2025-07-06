package bot.tg.util;

public class TextHelper {

    private TextHelper() {}

    public static String escapeMarkdown(String text) {
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("`", "\\`")
                .replace("[", "\\[");
    }
}
