package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For JSON. Fetch command.
 * Created on 2017/3/23.
 */
public class Fetch extends CommandWithResourceTemplate {
    @JsonCreator
    public Fetch(@JsonProperty("resourceTemplate") Resource resourceTemplate) {
        super(resourceTemplate);
    }

    @Override
    public CMD getCMD() {
        return CMD.FETCH;
    }
}
