package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created on 2017/5/10.
 */
public class Unsubscribe extends Command {
    private final String id;

    @JsonCreator
    public Unsubscribe(@JsonProperty("id") String id) {
        super();
        this.id = id;
    }

    @Override
    public CMD getCMD() {
        return CMD.UNSUBSCRIBE;
    }

    public String getId() {
        return id;
    }

}
