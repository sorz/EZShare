package EZShare.server.subscription;

import EZShare.entities.Resource;
import EZShare.entities.Server;
import EZShare.entities.Subscription;
import EZShare.networking.EZInputOutput;
import EZShare.networking.Result;
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
                .map(r -> r.ifErr(e -> LOGGER.fine("fail to connect with server: " + e)))
                .map(Result::ok).filter(Objects::nonNull)
                .map(s -> s.writeJSON(new Subscription(template, false)))
                .forEach(r -> r.ifErr(e -> LOGGER.fine("fail to subscribe: " + e)));
        return id;
    }

    @Override
    public void unsubscribe(String relayId) {
        // TODO
    }


    synchronized private Result<EZInputOutput> getConnection(Server server){
        if (connections.contains(server)) {
            EZInputOutput io = connections.get(server);
            if (io.isClosed())
                connections.remove(server);
            else
                return Result.of(io);
        }
        EZInputOutput io;
        try {
            io = new EZInputOutput(server);
        } catch (IOException e) {
            return new Result<>(e);
        }
        connections.put(server, io);
        return Result.of(io);
    }
}
