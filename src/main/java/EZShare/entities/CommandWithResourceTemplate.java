package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For JSON. A command with one resource template field.
 * Created by xierch on 2017/3/23.
 */
abstract class CommandWithResourceTemplate extends Command {
    private final Resource resourceTemplate;

    public Resource getResourceTemplate() {
        return resourceTemplate;
    }

    @JsonCreator
    CommandWithResourceTemplate(@JsonProperty("resourceTemplate") Resource resourceTemplate) {
        this.resourceTemplate = resourceTemplate;
    }
}
