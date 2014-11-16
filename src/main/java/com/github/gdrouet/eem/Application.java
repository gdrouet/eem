package com.github.gdrouet.eem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring boot application enabling vibe platform.
 */
@SpringBootApplication
public class Application {

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
