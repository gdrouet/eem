package com.github.gdrouet.eem;

import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.MetaBroadcaster;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * Decorates the servlet context by installing atmosphere servlet.
 */
@Configuration
public class AtmosphereWebConfigurer implements ServletContextInitializer {

    /**
     * <p>
     * Creates simple atmosphere servlet.
     * </p>
     *
     * @return the servlet
     */
    @Bean
    public AtmosphereServlet atmosphereServlet() {
        return new AtmosphereServlet();
    }

    /**
     * <p>
     * Gets framework from servlet.
     * </p>
     *
     * @return the framework
     */
    @Bean
    public AtmosphereFramework atmosphereFramework() {
        return atmosphereServlet().framework();
    }

    /**
     * Gets the broadcaster that push messages to all resources.
     *
     * @return the broadcaster
     */
    @Bean
    public MetaBroadcaster metaBroadcaster() {
        AtmosphereFramework framework = atmosphereFramework();
        return framework.metaBroadcaster();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        configureAthmosphere(atmosphereServlet(), servletContext);
    }

    /**
     * <p>
     * Configures atmosphere.
     * </p>
     *
     * @param servlet the servlet
     * @param servletContext the context
     */
    private void configureAthmosphere(final AtmosphereServlet servlet, final ServletContext servletContext) {
        ServletRegistration.Dynamic atmosphereServlet = servletContext.addServlet("atmosphereServlet", servlet);
        atmosphereServlet.setInitParameter(ApplicationConfig.ANNOTATION_PACKAGE, ManagedService.class.getPackage().getName());
        atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_CACHE, UUIDBroadcasterCache.class.getName());
        atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_SHARABLE_THREAD_POOLS, "true");
        atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_MESSAGE_PROCESSING_THREADPOOL_MAXSIZE, "10");
        atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_ASYNC_WRITE_THREADPOOL_MAXSIZE, "10");
        servletContext.addListener(new org.atmosphere.cpr.SessionSupport());
        atmosphereServlet.addMapping("/websocket/*");
        atmosphereServlet.setLoadOnStartup(0);
        atmosphereServlet.setAsyncSupported(true);
    }
}