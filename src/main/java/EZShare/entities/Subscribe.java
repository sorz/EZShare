package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Created on 2017/5/10.
 */
public class Subscribe extends CommandWithResourceTemplate {
    private final boolean relay;
    private final String id;

    @JsonCreator
    public Subscribe(@JsonProperty("resourceTemplate") Resource resourceTemplate,
                     @JsonProperty("relay") boolean relay) {
        super(resourceTemplate);
        this.relay = relay;
        id = UUID.randomUUID().toString();
    }

    @Override
    public CMD getCMD() {
        return CMD.SUBSCRIBE;
    }

    public String getId() {
        return id;
    }

    public boolean isRelay() {
        return relay;
    }
}
