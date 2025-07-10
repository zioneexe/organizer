package bot.tg.server;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;

public class JettyServer {

    public static void start() throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Server server = new Server(port);

        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");

        handler.addServlet(new ServletHolder(new OAuthCallbackServlet()), "/oauth2callback");
        handler.addServlet(new ServletHolder(new BroadcastServlet()), "/broadcast");

        server.setHandler(handler);
        server.start();
        System.out.println("Jetty розпочав роботу. Порт: " + port);
    }
}
