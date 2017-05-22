package EZShare.server.subscription;

import EZShare.entities.*;
import EZShare.networking.EZInputOutput;
import EZShare.server.ServerDaemon;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created on 2017/5/10.
 */
class SubscriptionRelayService implements RelayService {
    private final static Logger LOGGER = Logger.getLogger(ServerDaemon.class.getName());

    final private Consumer<Resource> updatedResourceConsumer;
    final private Hashtable<Server, EZInputOutput> connections = new Hashtable<>();
    final private Hashtable<String, Subscribe> subscriptions = new Hashtable<>();
    final private boolean secure;
    // TODO: share thread pool with SubscriptionService
    final private ExecutorService executorService = Executors.newCachedThreadPool();
    private Set<Server> servers = new HashSet<>();

    private boolean isRunning = true;

    SubscriptionRelayService(Consumer<Resource> updatedResourceConsumer, boolean isSecure) {
        this.updatedResourceConsumer = updatedResourceConsumer;
        secure = isSecure;
    }

    @Override
    public String subscribe(Resource template) {
        Subscribe subscribe = new Subscribe(template, false);
        subscriptions.put(subscribe.getId(), subscribe);
        connections.values().forEach(server -> {
            try {
                server.sendJSON(subscribe);
            } catch (IOException e) {
                LOGGER.fine(String.format(
                        "fail to subscribe with %s: %s", server, e));
            }
        });
        if (connections.isEmpty() && !servers.isEmpty())
            // we're the first one to subscribe.
            connectWithAllServers();
        return subscribe.getId();
    }

    @Override
    public void unsubscribe(String relayId) {
        subscriptions.remove(relayId);
        connections.values().forEach(server -> {
            try {
                server.sendJSON(new Unsubscribe(relayId));
            } catch (IOException e) {
                // ignore, no need to unsubscribe if connection closed.
            }
        });
    }

    void updateServerList(Set<Server> servers) {
        this.servers = servers;
        if (!subscriptions.isEmpty())
            connectWithAllServers();
    }

    synchronized private void connectWithAllServers() {
        // synchronized: to avoid connect one server twice.
        servers.stream()
                .filter(s -> !connections.containsKey(s))
                .forEach(this::connectWithNewServer);
    }

    private void connectWithNewServer(Server server) {
        EZInputOutput io;
        try {
            io = new EZInputOutput(server, secure, 0);
        } catch (IOException e) {
            LOGGER.fine(String.format(
                    "fail to connect with server %s: %s ", server, e));
            return;
        }
        connections.put(server, io);
        executorService.submit(() -> {
           try {
               subscriptions.values().forEach(io::uncheckedSendJSON);
               handleSubscriptionConnection(server, io);
           } catch (IOException e) {
               LOGGER.fine(String.format(
                       "error on handle subscription connection with %s: %s",
                       server, io));
               connections.remove(server);
           }
        });
    }

    private void handleSubscriptionConnection(Server server, EZInputOutput io)
            throws IOException {
        while (isRunning) {
            // try to read updated resource:
            try {
                Resource resource = io.readJSON(Resource.class);
                updatedResourceConsumer.accept(resource);
                continue;
            } catch (JsonMappingException e) {
                // ignore
            }
            // try to read response (success or error)
            Response response;
            try {
                response = io.readResponse();
                LOGGER.fine(String.format("%s: %s", server, response));
            } catch (JsonMappingException e) {
                LOGGER.info("unexpected message read from subscription " +
                        "connection with " + server + ", ignored.");
            }
        }
    }

    public void stop() {
        isRunning = false;
        executorService.shutdownNow();
        connections.values().forEach(EZInputOutput::close);
    }
}
