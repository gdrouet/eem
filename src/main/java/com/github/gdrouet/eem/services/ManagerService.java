package com.github.gdrouet.eem.services;

import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager which receives notifications from developers and just slap them in return.
 */
@ManagedService(path = "/websocket/manager")
public class ManagerService {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * <p>
     * Returning the message will broadcast it.
     * </p>
     *
     * @param m the pushed message
     * @return the broadcast message
     */
    @Message
    public String onMessage(final String m) {
        log.info("Broadcast {}", m);
        return m;
    }
}