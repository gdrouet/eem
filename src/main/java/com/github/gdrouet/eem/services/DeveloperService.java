package com.github.gdrouet.eem.services;

import org.atmosphere.config.service.Get;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Developer which push messages to manager.
 */
@ManagedService(path = "/websocket/developer")
public class DeveloperService {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Socket.
     */
    private Socket socket;

    /**
     * Connection initialized. Connect to manager.
     *
     * @param resource the resource which is connected
     * @throws IOException if connection to manager fails
     */
    @Get
    public void init(final AtmosphereResource resource) throws IOException {
        final Client client = ClientFactory.getDefault().newClient();
        final RequestBuilder request = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri("http://localhost:8080/websocket/manager")
                .encoder(new Encoder<String, Reader>() {
                    @Override
                    public Reader encode(final String s) {
                        return new StringReader(s);
                    }
                })
                .decoder(new Decoder<String, Reader>() {
                    @Override
                    public Reader decode(final Event type, final String s) {
                        return new StringReader(s);
                    }
                })
                .transport(Request.TRANSPORT.WEBSOCKET);

        socket = client.create();
        socket.on(new Function<Reader>() {
            @Override
            public void on(final Reader r) {
                try  {
                    final char[] c = new char[5];
                    r.read(c);
                    log.info("Receiving '{}'", new String(c));
                } catch (IOException ioe) {
                    log.error("", ioe);
                }
            }
        }).open(request.build());
    }

    /**
     * <p>
     * When a message is pushed by a developer, it's forwarded to the manager.
     * </p>
     *
     * @param m the message to forward
     * @throws IOException if push fails
     */
    @Message
    public void onMessage(final String m) throws IOException {
        if (socket == null) {
            throw new IllegalStateException("Running as manager.");
        }

        socket.fire(m);
    }
}