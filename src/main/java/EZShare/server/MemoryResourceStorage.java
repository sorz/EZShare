package EZShare.server;

import EZShare.entities.Resource;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URI;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Store resources on memory.
 * Created on 2017/3/27.
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

    @Override
    public Stream<Resource> templateQuery(Resource template) {
        return resources.values().stream()
                // The template channel equals (case sensitive) the resource channel:
                .filter(res -> res.getChannel().equals(template.getChannel()))
                // If the template contains an owner that is not "",
                // then the candidate owner must equal it (case sensitive):
                .filter(res -> template.getOwner().isEmpty()
                               || res.getOwner().equals(template.getOwner()))
                // Any tags present in the template also are present in the candidate (case insensitive):
                .filter(res -> res.getTags().containsAll(template.getTags()))
                // If the template contains a URI then the candidate URI matches (case sensitive)
                .filter(res -> template.getUri().isEmpty()
                               || res.getUri().equals(template.getUri()))
                .filter(res ->
                        // The candidate name contains the template name as a substring
                        // (for non "" template name)
                        // TODO: case insensitive?
                        (!template.getName().isEmpty()
                                && res.getName().contains(template.getName())) ||
                        // The candidate description contains the template description as a substring
                        // (for non "" template descriptions)
                        (!template.getDescription().isEmpty()
                                && res.getDescription().contains(template.getDescription())) ||
                        // The template description and name are both ""
                        (template.getName().isEmpty()
                                && template.getDescription().isEmpty())
                );
    }

}
