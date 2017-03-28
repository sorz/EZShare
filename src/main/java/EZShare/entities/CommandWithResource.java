package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For JSON. A command with one resource field.
 * Created by xierch on 2017/3/23.
 */
public abstract class CommandWithResource extends Command {
    private final Resource resource;

    public Resource getResource() {
        return resource;
    }

    @JsonCreator
    CommandWithResource(@JsonProperty("resource") Resource resource) {
        this.resource = resource;
    }
}
