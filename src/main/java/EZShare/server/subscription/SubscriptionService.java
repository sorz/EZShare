package EZShare.server.subscription;

import EZShare.entities.Resource;
import EZShare.entities.Server;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created on 2017/5/10.
 */
public class SubscriptionService {
    private final static int MAX_THREAD = 16;

    final private Set<Subscriber> subscribers = new HashSet<>();
    final private SubscriptionRelayService relayService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD);


    public SubscriptionService(Supplier<Set<Server>> serverListSupplier) {
        relayService = new SubscriptionRelayService(
                serverListSupplier, this::notifyUpdatedResource);
    }

    public Subscriber addSubscriber(Consumer<Resource> consumer) {
        Subscriber subscriber = new Subscriber(relayService, consumer);
        subscribers.add(subscriber);
        return subscriber;
    }

    public void removeSubscriber(Subscriber subscriber) {
        subscriber.unsubscribeAll();
        subscribers.remove(subscriber);
    }

    public void notifyUpdatedResource(Resource resource) {
        // TODO: add a queue to avoid blocking when thread pool is full.
        executorService.submit(() ->
            subscribers.forEach(s -> s.tryDelivery(resource))
        );
    }



}
