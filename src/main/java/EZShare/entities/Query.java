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

    /**
     * Clone a query.
     * @param query to clone.
     * @param relay override relay from query.
     */
    public Query(Query query, boolean relay) {
        this(new Resource(query.getResourceTemplate()), relay);
    }

    @Override
    public CMD getCMD() {
        return CMD.QUERY;
    }
}
