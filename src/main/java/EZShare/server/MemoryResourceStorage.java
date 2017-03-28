package EZShare.server;

import EZShare.entities.Resource;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URI;
import java.util.HashMap;

/**
 * Store resources on memory.
 * Created by xierch on 2017/3/27.
 */
public class MemoryResourceStorage implements ResourceStorage {
    private HashMap<Pair<String, URI>, Resource> resources = new HashMap<>();

    @Override
    public void put(String channel, URI uri, Resource resource) {
        resources.put(Pair.of(channel, uri), resource);
    }

    @Override
    public Resource get(String channel, URI uri) {
        return resources.get(Pair.of(channel, uri));
    }

    @Override
    public void remove(String channel, URI uri) {
        resources.remove(Pair.of(channel, uri));
    }

}
