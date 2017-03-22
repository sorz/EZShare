package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For JSON. PUBLISH command.
 * Remove the resource on the server.
 *
 * Created by xierch on 2017/3/23.
 */
public class Remove extends CommandWithResource {
    @JsonCreator
    public Remove(@JsonProperty("resource") Resource resource) {
        super(resource);
    }
}
