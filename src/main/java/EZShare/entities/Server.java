package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * For JSON. Include in EXCHANGE command.
 *
 * Created by xierch on 2017/3/29.
 */
public class Server {
    private final String hostname;
    private final int port;

    @JsonCreator
    public Server(@JsonProperty("hostname") String hostname,
                 @JsonProperty("port") int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Server &&
                ((Server) obj).getHostname().equals(getHostname()) &&
                ((Server) obj).getPort() == getPort();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 31)  // 11 + 31 = 42
                .append(getHostname())
                .append(getPort())
                .toHashCode();
    }

    @Override
    public String toString() {
        return getHostname() + ":" + getPort();
    }
}
