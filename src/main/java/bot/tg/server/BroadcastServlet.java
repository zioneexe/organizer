package bot.tg.server;

import bot.tg.service.BroadcastService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class BroadcastServlet extends HttpServlet {

    private final BroadcastService broadcastService;

    public BroadcastServlet() {
        this.broadcastService = new BroadcastService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");

        String text = req.getParameter("text");
        if (text == null || text.isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write("Відсутній 'text' параметр.");
            return;
        }

        broadcastService.broadcast(text);

        resp.setStatus(200);
        resp.getWriter().write("Розсилку проведено успішно.");
    }
}
