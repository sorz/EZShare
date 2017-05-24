package EZShare.server.subscription;

import EZShare.entities.Resource;

import java.util.Hashtable;
import java.util.function.Consumer;

/**
 * One subscriber with one or more its subscriptions.
 * Created by xierch on 2017/5/10.
 */
public class Subscriber {
    private final RelayService relayService;
    private final Consumer<Resource> subscriber;
    private final Hashtable<String, Subscription> subscriptions = new Hashtable<>();

    Subscriber(RelayService relayService, Consumer<Resource> subscriber) {
        this.relayService = relayService;
        this.subscriber = subscriber;
    }

    public void subscribe(String id, Resource template, boolean relay) {
        Subscription subscription = new Subscription(template);
        subscriptions.put(id, subscription);
        // There are two IDs on relayed subscriptions,
        // the "id" is one that client sent to us. And the "relayId" is one that
        // we sent to other servers. This is returned by RelayService and used by
        // RelayService.
        if (relay)
            subscription.relayId = relayService.subscribe(template);
    }

    public void unsubscribe(String id) {
        Subscription s = subscriptions.remove(id);
        if (s.relayId != null)
            relayService.unsubscribe(s.relayId);
    }

    public void unsubscribeAll() {
        subscriptions.keySet().forEach(this::unsubscribe);
    }

    private boolean isDeliverable(Resource resource) {
        return subscriptions.values().stream()
                .map(Subscription::getTemplate)
                .anyMatch(resource::matchWithTemplate);
    }

    void tryDelivery(Resource resource) {
        if (!isDeliverable(resource))
            return;
        subscriber.accept(resource);
    }

    public boolean isEmpty() {
        return subscriptions.isEmpty();
    }

    private static class Subscription {
        Resource template;
        String relayId;

        Subscription(Resource template) {
            this.template = template;
        }

        Resource getTemplate() {
            return template;
        }
    }
}
