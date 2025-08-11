package bot.tg.dto;

import lombok.Getter;

@Getter
public enum SupportedTimeZone {
    KYIV_OLD("Europe/Kiev", "🇺🇦 Київ"),
    KYIV("Europe/Kyiv", "🇺🇦 Київ"),
    WARSAW("Europe/Warsaw", "🇵🇱 Варшава"),
    LONDON("Europe/London", "🇬🇧 Лондон"),
    NEW_YORK("America/New_York", "🇺🇸 Нью-Йорк"),
    TOKYO("Asia/Tokyo", "🇯🇵 Токіо"),
    UTC("UTC", "🌐 UTC");

    private final String zoneId;
    private final String displayName;

    SupportedTimeZone(String zoneId, String displayName) {
        this.zoneId = zoneId;
        this.displayName = displayName;
    }

    public static SupportedTimeZone fromZoneId(String zoneId) {
        for (SupportedTimeZone timeZone : values()) {
            if (timeZone.zoneId.equals(zoneId)) return timeZone;
        }
        throw new IllegalArgumentException("Unsupported zoneId: " + zoneId);
    }

    public static SupportedTimeZone getDefault() {
        return KYIV;
    }
}
