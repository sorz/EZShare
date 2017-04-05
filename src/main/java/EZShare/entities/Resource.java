package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * For JSON. Include in many JSON command, e.g. PUBLISH, REMOVE, ...
 * Created by xierch on 2017/3/22.
 */
public class Resource {
    private String name;
    private List<String> tags;
    private String description;
    private String uri;
    private String channel;
    private String owner;
    private String ezserver;
    // resourceSize only appears in FETCH response.
    // The bytes of the file.
    private long resourceSize;

    @JsonCreator
    public Resource() {
        // Following fields cannot be null.
        name = "";
        tags = new ArrayList<>();
        description = "";
        uri = "";
        channel = "";
        owner = "";
        // Following default to null or 0.
        ezserver = null;
        resourceSize = 0;
    }

    /**
     * Clone a resource.
     * @param resource to clone.
     */
    public Resource(Resource resource) {
        this.name = resource.getName();
        this.tags = new ArrayList<>(resource.getTags());
        this.description = resource.getDescription();
        this.uri = resource.getUri();
        this.channel = resource.getChannel();
        this.owner = resource.getOwner();
        this.ezserver = resource.getEzserver();
        this.resourceSize = resource.getResourceSize();
    }

    @NotNull
    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public List<String> getTags() {
        if (tags == null)
            tags = new ArrayList<>();
        return tags;
    }

    public void setTags(@NotNull List<String> tags) {
        this.tags = tags;
    }

    @NotNull
    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    @NotNull
    public String getUri() {
        return uri == null ? "" : uri;
    }

    @Nullable
    @JsonIgnore
    public URI getNormalizedUri() {
        try {
            return new URI(getUri());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public void setUri(@NotNull String uri) {
        this.uri = uri;
    }

    @NotNull
    public String getChannel() {
        return channel == null ? "" : channel;
    }

    public void setChannel(@NotNull String channel) {
        this.channel = channel;
    }

    @NotNull
    public String getOwner() {
        return owner == null ? "" : owner;
    }

    public void setOwner(@NotNull String owner) {
        this.owner = owner;
    }

    @Nullable
    public String getEzserver() {
        return ezserver;
    }

    public void setEzserver(String ezserver) {
        this.ezserver = ezserver;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public long getResourceSize() {
        return resourceSize;
    }

    public Resource setResourceSize(long resourceSize) {
        this.resourceSize = resourceSize;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Resource [%s]\n",
                getName().isEmpty() ? "UNNAMED" : getName()));
        builder.append(String.format("| URI: %s\n", getUri()));
        if (!getChannel().isEmpty())
            builder.append(String.format("| Channel: %s\n", getChannel()));
        if (!getOwner().isEmpty())
            builder.append(String.format("| Owner: %s\n", getOwner()));
        if (getEzserver() != null && !getEzserver().isEmpty())
            builder.append(String.format("| Server: %s\n", getEzserver()));
        if (!getTags().isEmpty()) {
            builder.append("| Tags: ");
            getTags().stream().map(t -> t + ", ").forEach(builder::append);
            builder.delete(builder.length() - 2, builder.length());
            builder.append("\n");
        }
        if (!getDescription().isEmpty())
            builder.append(String.format("| %s\n", getDescription()));
        return builder.toString();
    }
}
