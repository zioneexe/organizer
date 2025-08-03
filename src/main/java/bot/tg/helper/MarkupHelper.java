package bot.tg.helper;

public class MarkupHelper {

    private MarkupHelper() {
    }

    public static String escapeMarkdown(String text) {
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("`", "\\`")
                .replace("[", "\\[");
    }
}
