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

    public void subscribe(String id, Resource template) {
        subscribe(id, template, false);
    }

    public void subscribe(String id, Resource template, boolean relay) {
        Subscription subscription = new Subscription(template);
        subscriptions.put(id, subscription);
        if (relay)
            subscription.relayId = relayService.subscribe(template);
    }

    public int unsubscribe(String id) {
        Subscription s = subscriptions.remove(id);
        if (s.relayId != null)
            relayService.unsubscribe(s.relayId);
        return s.count;
    }

    void unsubscribeAll() {
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
        subscriptions.values().stream()
                .filter(s -> resource.matchWithTemplate(s.getTemplate()))
                .forEach(Subscription::increaseCounter);
    }


    private static class Subscription {
        Resource template;
        int count;
        String relayId;

        Subscription(Resource template) {
            this.template = template;
        }

        synchronized void increaseCounter() {
            count += 1;
        }

        Resource getTemplate() {
            return template;
        }
    }
}
