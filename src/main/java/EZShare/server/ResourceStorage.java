package EZShare.server;

import EZShare.entities.Resource;

import java.net.URI;
import java.util.stream.Stream;

/**
 * Put, update, lookup resources.
 * Created on 2017/3/27.
 */
public interface ResourceStorage {
    public void put(String channel, URI uri, Resource resource);
    public Resource get(String channel, URI uri);
    void remove(String channel, URI uri);

    /**
     * Add a new resource or replace the old resource.
     * It will find old resource with key (channel, uri). If not found,
     * add resource directly. If found, only replace the old one when
     * the owner of old & new resource are the same.
     * @param resource to add or replace.
     * @return false if failed to replace (due to different owner).
     */
    public default boolean updateResource(Resource resource) {
        URI uri = resource.getNormalizedUri();
        Resource oldResource = get(resource.getChannel(), uri);
        if (oldResource != null && !oldResource.getOwner().equals(resource.getOwner()))
            return false;
        put(resource.getChannel(), uri, resource);
        return true;
    }

    public Stream<Resource> templateQuery(Resource template);
}
