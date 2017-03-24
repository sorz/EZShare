package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For JSON. SHARE command.
 * Create a resource on the server, URI must be file.
 *
 * Created by xierch on 2017/3/23.
 */
public class Share extends CommandWithResource {
    private String secret;

    public String getSecret() {
        return secret;
    }

    @JsonCreator
    public Share(@JsonProperty("resource") Resource resource,
                 @JsonProperty("secret") String secret) {
        super(resource);
        this.secret = secret;
    }

    @Override
    public String getCommandName() {
        return "SHARE";
    }
}
