package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;
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

    @NotNull
    public String getHostname() {
        return hostname == null ? "" : hostname;
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

    @JsonIgnore
    public boolean isValid() {
        return !getHostname().isEmpty() && (getPort() & ~0xffff) == 0 && getPort() > 0;
    }

    @Override
    public String toString() {
        return getHostname() + ":" + getPort();
    }
}
