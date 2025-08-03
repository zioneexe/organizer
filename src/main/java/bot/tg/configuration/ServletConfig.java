package bot.tg.configuration;

import bot.tg.servlet.BroadcastServlet;
import bot.tg.servlet.HealthServlet;
import bot.tg.servlet.OAuthCallbackServlet;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ServletConfig {

    private final OAuthCallbackServlet oAuthCallbackServlet;
    private final BroadcastServlet broadcastServlet;
    private final HealthServlet healthServlet;

    @Bean
    public ServletRegistrationBean<OAuthCallbackServlet> oAuthCallbackServletRegistration() {
        return new ServletRegistrationBean<>(oAuthCallbackServlet, "/oauth2callback");
    }

    @Bean
    public ServletRegistrationBean<BroadcastServlet> broadcastServletRegistration() {
        return new ServletRegistrationBean<>(broadcastServlet, "/broadcast");
    }

    @Bean
    public ServletRegistrationBean<HealthServlet> healthServletRegistration() {
        return new ServletRegistrationBean<>(healthServlet, "/health");
    }
}
