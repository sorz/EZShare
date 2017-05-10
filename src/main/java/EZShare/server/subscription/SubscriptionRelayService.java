package EZShare.server.subscription;

import EZShare.entities.Resource;
import EZShare.entities.Server;
import EZShare.entities.Subscription;
import EZShare.entities.Unsubscribe;
import EZShare.networking.EZInputOutput;
import EZShare.server.ServerDaemon;

import java.io.IOException;
import java.util.*;
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
            io = new EZInputOutput(server);
        } catch (IOException e) {
            LOGGER.fine(String.format(
                    "fail to connect with server %s: %s ", server, e));
            return null;
        }
        connections.put(server, io);
        return io;
    }
}
