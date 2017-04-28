package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * For JSON. EXCHANGE command.
 * Receive a list of EZShare host:port names.
 *
 * Created on 2017/3/29.
 */
public class Exchange extends Command {
    private final List<Server> serverList;

    @JsonCreator
    public Exchange(@JsonProperty("serverList") List<Server> serverList) {
        this.serverList = serverList;
    }

    public List<Server> getServerList() {
        return serverList;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("cmd.EXCHANGE {");
        getServerList().forEach(s -> builder.append(s + " "));
        builder.delete(builder.length() - 1, builder.length());
        builder.append("}");
        return builder.toString();
    }

    @Override
    public CMD getCMD() {
        return CMD.EXCHANGE;
    }
}
