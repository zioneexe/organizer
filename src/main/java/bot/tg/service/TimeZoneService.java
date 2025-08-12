package bot.tg.service;

import bot.tg.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeZoneService {

    private static final String TIMEZONE_API_URL = "https://maps.googleapis.com/maps/api/timezone/json";
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final UserRepository userRepository;
    private final String googleTimeZoneApiKey;

    public String resolveZoneId(double latitude, double longitude) {
        try {
            log.info("Determining time zone for coordinates: {}, {}", latitude, longitude);
            Long timestamp = Instant.now().getEpochSecond();
            GenericUrl url = new GenericUrl(TIMEZONE_API_URL);

            url.put("location", latitude + "," + longitude);
            url.put("timestamp", timestamp);
            url.put("key", googleTimeZoneApiKey);

            HttpRequestFactory factory = HTTP_TRANSPORT.createRequestFactory();
            HttpRequest request = factory.buildGetRequest(url);

            HttpResponse response = request.execute();

            try (InputStream inputStream = response.getContent()) {
                JsonNode root = OBJECT_MAPPER.readTree(inputStream);

                if ("OK".equals(root.path("status").asText())) {
                    String zoneId = root.path("timeZoneId").asText();
                    log.info("Determined time zone: {}", zoneId);
                    return zoneId;
                } else {
                    String errorMessage = root.path("errorMessage").asText("");
                    log.error("TimeZone API error: {} — {}", root.path("status").asText(), errorMessage);
                    throw new RuntimeException("TimeZone API error: " + root.path("status").asText()
                            + (errorMessage.isEmpty() ? "" : " — " + errorMessage));
                }
            }
        } catch (Exception e) {
            log.error("Failed to determine time zone", e);
            throw new RuntimeException("Failed to resolve time zone", e);
        }
    }

    public ZoneId getUserZoneId(Long userId) {
        String userTimeZone = userRepository.getById(userId).getTimeZone();
        return userTimeZone == null || userTimeZone.isBlank() ?
                ZoneOffset.UTC : ZoneId.of(userTimeZone);
    }
}
