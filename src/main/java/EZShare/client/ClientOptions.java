package EZShare.client;

import EZShare.entities.Command;
import javafx.util.Pair;

import java.net.URI;
import java.util.List;

/**
 *  Store options for running a client.
 * Created by xierch on 2017/3/22.
 */
public class ClientOptions {
    private Command.CMD command;
    private String channel;
    private String description;
    private String host;
    private String name;
    private String owner;
    private int port;
    private String secret;
    private List<Pair<String, Integer>> servers;
    private String share;
    private List<String> tags;
    private URI uri;

    public ClientOptions setCommand(Command.CMD command) {
        this.command = command;
        return this;
    }

    public ClientOptions setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public ClientOptions setDescription(String description) {
        this.description = description;
        return this;
    }

    public ClientOptions setHost(String host) {
        this.host = host;
        return this;
    }

    public ClientOptions setName(String name) {
        this.name = name;
        return this;
    }

    public ClientOptions setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public ClientOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public ClientOptions setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public ClientOptions setServers(List<Pair<String, Integer>> servers) {
        this.servers = servers;
        return this;
    }

    public ClientOptions setShare(String share) {
        this.share = share;
        return this;
    }

    public ClientOptions setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public ClientOptions setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public Command.CMD getCommand() {
        return command;
    }

    public String getChannel() {
        return channel;
    }

    public String getDescription() {
        return description;
    }

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public int getPort() {
        return port;
    }

    public String getSecret() {
        return secret;
    }

    public List<Pair<String, Integer>> getServers() {
        return servers;
    }

    public String getShare() {
        return share;
    }

    public List<String> getTags() {
        return tags;
    }

    public URI getUri() {
        return uri;
    }
}
