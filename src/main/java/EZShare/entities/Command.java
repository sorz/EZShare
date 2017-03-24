package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * For JSON. Base class for all command.
 * Created by xierch on 2017/3/23.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CUSTOM, include=JsonTypeInfo.As.PROPERTY, property="command")
@JsonTypeIdResolver(CommandTypeIdResolver.class)
public abstract class Command {
    public enum CMD {
        PUBLISH, REMOVE, SHARE, QUERY, FETCH, EXCHANGE
    }

    @JsonIgnore
    public abstract CMD getCMD();

}
