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
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Created on 2017/5/10.
 */
class SubscriptionRelayService implements RelayService {
    private final static Logger LOGGER = Logger.getLogger(ServerDaemon.class.getName());

    final private Supplier<Set<Server>> serverListSupplier;
    final private Consumer<Resource> updatedResourceConsumer;
    final private Hashtable<String, Set<Server>> subscribedServers = new Hashtable<>();
    final private Hashtable<Server, EZInputOutput> connections = new Hashtable<>();
    // TODO: share thread pool with SubscriptionService
    final private ExecutorService executorService = Executors.newCachedThreadPool();

    private boolean isRunning = true;

    SubscriptionRelayService(Supplier<Set<Server>> serverListSupplier,
                             Consumer<Resource> updatedResourceConsumer) {
        this.serverListSupplier = serverListSupplier;
        this.updatedResourceConsumer = updatedResourceConsumer;
    }

    @Override
    public String subscribe(Resource template) {
        String id = UUID.randomUUID().toString();
        subscribedServers.put(id, new HashSet<>());
        serverListSupplier.get().stream()
                .map(this::getConnection)
                .filter(Objects::nonNull)
                .forEach(server -> {
                    try {
                        server.sendJSON(new Subscription(template, false));
                    } catch (IOException e) {
                        LOGGER.fine(String.format(
                                "fail to subscribe with %s: %s", server, e));
                    }
                });
        return id;
    }

    @Override
    public void unsubscribe(String relayId) {
        Set<Server> servers;
        synchronized (subscribedServers) {
            servers = subscribedServers.remove(relayId);
        }
        if (servers == null)
            return;
        servers.stream()
                .map(connections::get)
                .filter(Objects::nonNull)
                .filter(EZInputOutput::isOpened)
                .forEach(server -> {
                    try {
                        server.sendJSON(new Unsubscribe(relayId));
                    } catch (IOException e) {
                        // ignore, no need to unsubscribe if connection closed.
                    }
                });
    }

    synchronized private EZInputOutput getConnection(Server server){
        if (connections.contains(server)) {
            EZInputOutput io = connections.get(server);
            if (io.isClosed())
                connections.remove(server);
            else
                return io;
        }
        EZInputOutput io;
        try {
            io = new EZInputOutput(server, 0);
        } catch (IOException e) {
            LOGGER.fine(String.format(
                    "fail to connect with server %s: %s ", server, e));
            return null;
        }
        connections.put(server, io);
        executorService.submit(() -> {
           try {
               handleSubscriptionConnection(server, io);
           } catch (IOException e) {
               LOGGER.fine(String.format(
                       "error on handle subscription connection with %s: %s",
                       server, io));
               connections.remove(server);
           }
        });
        return io;
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
            } catch (JsonMappingException e) {
                LOGGER.info("unexpected message read from subscription " +
                        "connection with " + server + ", ignored.");
                continue;
            }
            if (response.isSuccess()) {
                String id = response.getId();
                if (id == null || id.isEmpty()) {
                    LOGGER.info("blank id found on response from " + server);
                    continue;
                }
                synchronized (subscribedServers) {
                    if (subscribedServers.contains(id)) {
                        subscribedServers.get(id).add(server);
                        LOGGER.fine(id + " subscribed with " + server);
                    } else {
                        LOGGER.info(String.format("subscription '%s' not exist", id));
                    }
                }
            } else {
                LOGGER.info("operation fail on subscription with server " +
                        server + ": " + response.getErrorMessage());
            }
        }
    }

    public void stop() {
        isRunning = false;
        executorService.shutdownNow();
        connections.values().forEach(EZInputOutput::close);
    }
}
