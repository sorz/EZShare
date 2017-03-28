package EZShare.server;

import EZShare.entities.Resource;
import com.sun.istack.internal.Nullable;

import java.net.URI;

/**
 * Put, update, lookup resources.
 * Created by xierch on 2017/3/27.
 */
public interface ResourceStorage {
    public void put(String channel, URI uri, Resource resource);
    @Nullable
    public Resource get(String channel, URI uri);
    void remove(String channel, URI uri);
}
