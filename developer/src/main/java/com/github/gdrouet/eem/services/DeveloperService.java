/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Guillaume DROUET
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.github.gdrouet.eem.services;

import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springsource.loaded.ReloadEventProcessorPlugin;
import org.springsource.loaded.agent.SpringLoadedPreProcessor;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Developer which push messages to manager.
 */
@Component
public class DeveloperService {

    /**
     * Working message.
     */
    public static final String WORKING = "working";

    /**
     * Slap.
     */
    public static final String SLAP = "slap";

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Developer name.
     */
    @Value("#{systemProperties.name}")
    private String name;

    /**
     * Socket.
     */
    private Socket socket;

    /**
     * Connection initialized. Connect to manager.
     *
     * @throws java.io.IOException if connection to manager fails
     */
    @PostConstruct
    public void init() throws IOException {
        final Client client = ClientFactory.getDefault().newClient();
        final RequestBuilder request = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri("http://localhost:8080/websocket/manager")
                .encoder(new Encoder<String, String>() {
                    @Override
                    public String encode(final String s) {
                        return s;
                    }
                })
                .decoder(new Decoder<String, String>() {
                    @Override
                    public String decode(final Event e, final String s) {
                        return s;
                    }
                })
                .transport(Request.TRANSPORT.WEBSOCKET);

        socket = client.create();
        socket.on((s) -> {
            final int separator = s.toString().indexOf('|');
            final String body = s.toString().substring(separator + 1);
            final int frameDelimiter = body.indexOf(":");
            final String frame = body.substring(0, frameDelimiter);

            switch (frame) {
                case WORKING:
                    log.info("Manager notified");
                    break;
                case SLAP:
                    final String dev = body.substring(frameDelimiter + 1);
                    if (name.equals(dev)) {
                        log.info("##############################################################");
                        log.info("###### !! HURRY UP, YOUR MANAGER IS NOW SLAPPING YOU !! ######");
                        log.info("##############################################################");
                    } else {
                        log.info("Your friend '{}' has been slapped by an evil manager.", dev);
                    }
                    break;
            }
        }).open(request.build());

        SpringLoadedPreProcessor.registerGlobalPlugin(new PushPlugin());
    }

    /**
     * A plugin pushing messages to the socket when reload event occurs.
     */
    public class PushPlugin implements ReloadEventProcessorPlugin {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean shouldRerunStaticInitializer(final String typeName, final Class<?> clazz, final String encodedTimestamp) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reloadEvent(final String typeName, final Class<?> clazz, final String encodedTimestamp) {
            try {
                socket.fire(WORKING + ":" + name);
            } catch (IOException ioe) {
                log.error("", ioe);
            }
        }
    }
}