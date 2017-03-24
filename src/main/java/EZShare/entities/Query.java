package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For JSON. QUERY command.
 * Created by xierch on 2017/3/23.
 */
public class Query extends CommandWithResourceTemplate {
    private final boolean relay;

    public boolean isRelay() {
        return relay;
    }

    @JsonCreator
    public Query(@JsonProperty("resourceTemplate") Resource resourceTemplate,
                 @JsonProperty("relay") boolean relay) {
        super(resourceTemplate);
        this.relay = relay;
    }

    @Override
    public CMD getCMD() {
        return CMD.QUERY;
    }
}
