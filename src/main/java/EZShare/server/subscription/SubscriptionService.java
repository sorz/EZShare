package EZShare.server.subscription;

import EZShare.entities.Resource;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created on 2017/5/10.
 */
public class SubscriptionService {
    private final static int MAX_THREAD = 16;

    final private Set<Subscriber> subscribers = new HashSet<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD);


    public SubscriptionService() {

    }

    public Subscriber addSubscriber(Consumer<Resource> consumer) {
        Subscriber subscriber = new Subscriber(consumer);
        subscribers.add(subscriber);
        return subscriber;
    }

    public void removeSubscriber(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public void notifyUpdatedResource(Resource resource) {
        // TODO: add a queue to avoid blocking when thread pool is full.
        executorService.submit(() ->
            subscribers.forEach(s -> s.tryDelivery(resource))
        );
    }



}
