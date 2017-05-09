package EZShare.server.subscription;

import EZShare.entities.Resource;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * One subscriber with one or more its subscriptions.
 * Created by xierch on 2017/5/10.
 */
public class Subscriber {
    private final Consumer<Resource> subscriber;
    private final Hashtable<String, Resource> subscriptions;
    private final Hashtable<String, Integer> deliveryCount;

    Subscriber(Consumer<Resource> subscriber) {
        this.subscriber = subscriber;
        subscriptions = new Hashtable<>();
        deliveryCount = new Hashtable<>();
    }

    public void subscribe(String id, Resource template) {
        subscriptions.put(id, template);
    }

    public void subscribe(String id, Resource template, boolean relay) {
        subscriptions.put(id, template);
        if (relay) {
            // TODO
        }
    }

    public int unsubscribe(String id) {
        subscriptions.remove(id);
        int count = deliveryCount.getOrDefault(id, 0);
        deliveryCount.remove(id);
        return count;
    }

    private boolean isDeliverable(Resource resource) {
        return subscriptions.entrySet().stream()
                .map(Map.Entry::getValue)
                .anyMatch(resource::matchWithTemplate);
    }

    void tryDelivery(Resource resource) {
        if (!isDeliverable(resource))
            return;
        subscriber.accept(resource);
        subscriptions.entrySet().stream()
                .filter(entry -> resource.matchWithTemplate(entry.getValue()))
                .map(Map.Entry::getKey)
                .forEach(id -> deliveryCount.put(id,
                        1 + deliveryCount.getOrDefault(id, 0)));
    }
}
