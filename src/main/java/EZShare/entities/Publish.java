package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For JSON. PUBLISH command.
 * Create a resource on the server.
 *
 * Created by xierch on 2017/3/23.
 */
public class Publish extends Command {
    private final Resource resource;

    public Resource getResource() {
        return resource;
    }

    @JsonCreator
    public Publish(@JsonProperty("resource") Resource resource) {
        this.resource = resource;
    }


}
