package bot.tg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.Instant;

public class TimeZoneService {

    private static final Logger logger = LoggerFactory.getLogger(TimeZoneService.class);

    private static final String TIMEZONE_API_URL = "https://maps.googleapis.com/maps/api/timezone/json";
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String API_KEY = System.getenv("GOOGLE_TIMEZONE_API_KEY");

    public static String resolveZoneId(double latitude, double longitude) {
        try {
            logger.info("Визначення часової зони для координат: {}, {}", latitude, longitude);

            long timestamp = Instant.now().getEpochSecond();
            GenericUrl url = new GenericUrl(TIMEZONE_API_URL);
            url.put("location", latitude + "," + longitude);
            url.put("timestamp", timestamp);
            url.put("key", API_KEY);

            HttpRequestFactory factory = HTTP_TRANSPORT.createRequestFactory();
            HttpRequest request = factory.buildGetRequest(url);

            HttpResponse response = request.execute();

            try (InputStream inputStream = response.getContent()) {
                JsonNode root = OBJECT_MAPPER.readTree(inputStream);

                if ("OK".equals(root.path("status").asText())) {
                    String zoneId = root.path("timeZoneId").asText();
                    logger.info("Визначена часова зона: {}", zoneId);
                    return zoneId;
                } else {
                    String errorMessage = root.path("errorMessage").asText("");
                    logger.error("Помилка TimeZone API: {} — {}", root.path("status").asText(), errorMessage);
                    throw new RuntimeException("TimeZone API error: " + root.path("status").asText()
                            + (errorMessage.isEmpty() ? "" : " — " + errorMessage));
                }
            }
        } catch (Exception e) {
            logger.error("Не вдалося визначити часову зону", e);
            throw new RuntimeException("Failed to resolve time zone", e);
        }
    }
}
