package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For JSON. PUBLISH command.
 * Create a resource on the server.
 *
 * Created on 2017/3/23.
 */
public class Publish extends CommandWithResource {
    @JsonCreator
    public Publish(@JsonProperty("resource") Resource resource) {
        super(resource);
    }

    @Override
    public CMD getCMD() {
        return CMD.PUBLISH;
    }
}
