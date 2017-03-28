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


    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(@NotNull List<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    public String getUri() {
        return uri;
    }

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

    public String getChannel() {
        return channel;
    }

    public void setChannel(@NotNull String channel) {
        this.channel = channel;
    }

    public String getOwner() {
        return owner;
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
}
