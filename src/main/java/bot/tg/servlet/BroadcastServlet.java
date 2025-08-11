package bot.tg.servlet;

import bot.tg.service.BroadcastService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BroadcastServlet extends HttpServlet {

    private final BroadcastService broadcastService;

    private static final String SUCCESSFUL_BROADCAST = "Розсилку проведено успішно.";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");

        String body = req.getReader().lines().collect(Collectors.joining("\n"));
        ObjectMapper mapper = new ObjectMapper();
        String text = mapper.readTree(body).get("text").asText();

        broadcastService.broadcast(text);

        resp.setStatus(200);
        resp.getWriter().write(SUCCESSFUL_BROADCAST);
    }
}
