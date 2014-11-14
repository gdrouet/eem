package com.github.gdrouet.eem;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.vibe.platform.Action;
import org.atmosphere.vibe.platform.server.ServerHttpExchange;
import org.atmosphere.vibe.platform.server.ServerWebSocket;
import org.atmosphere.vibe.platform.server.atmosphere2.VibeAtmosphereServlet;
import org.atmosphere.vibe.server.DefaultServer;
import org.atmosphere.vibe.server.Server;
import org.atmosphere.vibe.server.ServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.ServletRegistration;

/**
 * Spring boot application enabling vibe platform.
 */
@SpringBootApplication
public class Application {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    /**
     * <p>
     * Creates a vibe server instance that broadcast all messages sent on channel.
     * </p>
     *
     * @return the vibe server
     */
    @Bean
    public Server server() {
        final Server server = new DefaultServer();
        server.socketAction((ServerSocket socket) -> {
            socket.on("chat", (Object data) -> {
                LOG.info(data + "received from client");
                server.all().send("chat", data.toString() + " from server");
            });
        });

        return server;
    }

    /**
     * <p>
     * Creates a registration for vibe server mapped to /vibe/* pattern.
     * </p>
     *
     * @param server the vibe server
     * @return the registration
     */
    @Bean
    public ServletRegistrationBean servletRegistrationBean(final Server server) {
        return new ServletRegistrationBean(new VibeAtmosphereServlet() {
            @Override
            protected Action<ServerHttpExchange> httpAction() {
                return server.httpAction();
            }

            @Override
            protected Action<ServerWebSocket> wsAction() {
                return server.wsAction();
            }
        }, "/vibe/*") {
            @Override
            protected void configure(final ServletRegistration.Dynamic reg) {
                super.configure(reg);
                reg.setAsyncSupported(true);
                reg.setInitParameter(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR, Boolean.TRUE.toString());
            }
        };
    }

    /**
     * <p>
     * Bootstrap.
     * </p>
     *
     * @param args arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
