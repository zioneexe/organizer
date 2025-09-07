package bot.tg.util;

public class MarkupAdjuster {

    private MarkupAdjuster() {
    }

    public static String escapeMarkdown(String text) {
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("`", "\\`")
                .replace("[", "\\[");
    }
}
