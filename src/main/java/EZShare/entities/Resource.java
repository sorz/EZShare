package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonInclude;

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
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private long resourceSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getEzserver() {
        return ezserver;
    }

    public void setEzserver(String ezserver) {
        this.ezserver = ezserver;
    }

    public long getResourceSize() {
        return resourceSize;
    }

    public Resource setResourceSize(long resourceSize) {
        this.resourceSize = resourceSize;
        return this;
    }
}
