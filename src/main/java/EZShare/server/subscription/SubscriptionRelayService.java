package EZShare.server.subscription;

import EZShare.entities.Resource;
import EZShare.entities.Server;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created on 2017/5/10.
 */
class SubscriptionRelayService implements RelayService {
    final private Supplier<Set<Server>> serverListSupplier;
    final private Consumer<Resource> updatedResourceConsumer;

    SubscriptionRelayService(Supplier<Set<Server>> serverListSupplier,
                             Consumer<Resource> updatedResourceConsumer) {
        this.serverListSupplier = serverListSupplier;
        this.updatedResourceConsumer = updatedResourceConsumer;
    }

    @Override
    public String subscribe(Resource template) {
        String id = UUID.randomUUID().toString();
        // TODO
        return id;
    }

    @Override
    public void unsubscribe(String relayId) {
        // TODO
    }
}
