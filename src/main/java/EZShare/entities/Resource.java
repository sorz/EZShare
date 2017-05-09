package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * For JSON. Include in many JSON command, e.g. PUBLISH, REMOVE, ...
 * Created on 2017/3/22.
 */
public class Resource {
    private static final String[] SIZE_UNITS =
            new String[] { "Bytes", "KiB", "MiB", "GiB", "TiB" };


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

    public String getName() {
        return name == null ? "" : name.trim();
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        if (tags == null)
            tags = new ArrayList<>();
        return tags.stream().map(String::trim).collect(Collectors.toList());
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description == null ? "" : description.trim();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUri() {
        return uri == null ? "" : uri.trim();
    }

    @JsonIgnore
    public URI getNormalizedUri() {
        try {
            return new URI(getUri());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getChannel() {
        return channel == null ? "" : channel.trim();
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getOwner() {
        return owner == null ? "" : owner.trim();
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getEzserver() {
        return ezserver == null? null : ezserver.trim();
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

    @JsonIgnore
    public String getReadableResourceSize() {
        // Reference:
        // https://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
        if (getResourceSize() <= 0)
            return "0";
        int digitGroups = (int) (Math.log10(getResourceSize()) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(getResourceSize() / Math.pow(1024, digitGroups))
                + " " + SIZE_UNITS[digitGroups];
    }

    /**
     * Test this resource with template.
     * @param template to match.
     * @return whether it match with template.
     */
    public boolean matchWithTemplate(Resource template) {
        // The template channel equals (case sensitive) the resource channel
        if (!getChannel().equals(template.getChannel()))
            return false;
        // If the template contains an owner that is not "",
        // then the candidate owner must equal it (case sensitive)
        if ((!template.getOwner().isEmpty())
                && (!getOwner().equals(template.getOwner())))
            return false;
        // Any tags present in the template also are present in the candidate (case insensitive):
        if (!getTags().stream().map(String::toLowerCase)
                .collect(Collectors.toSet()).containsAll(
                        template.getTags().stream()
                                .map(String::toLowerCase)
                                .collect(Collectors.toSet())))
            return false;
        // If the template contains a URI then the candidate URI matches (case sensitive)
        if (!(template.getUri().isEmpty() || getUri().equals(template.getUri())))
            return false;
        return (
                // The candidate name contains the template name as a substring
                // (for non "" template name)
                // TODO: case insensitive?
                (!template.getName().isEmpty()
                        && getName().contains(template.getName())) ||
                // The candidate description contains the template description as a substring
                // (for non "" template descriptions)
                (!template.getDescription().isEmpty()
                        && getDescription().contains(template.getDescription())) ||
                // The template description and name are both ""
                (template.getName().isEmpty()
                        && template.getDescription().isEmpty())
        );
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
        if (getResourceSize() != 0)
            builder.append(String.format("| Size: %s\n", getReadableResourceSize()));
        if (!getDescription().isEmpty())
            builder.append(String.format("| %s\n", getDescription()));
        return builder.toString();
    }
}
