package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For JSON. The end of a QUERY and FETCH response.
 * Created by xierch on 2017/3/23.
 */
public class ResultSize {
    private final int resultSize;

    public int get() {
        return resultSize;
    }

    @JsonCreator
    public ResultSize(@JsonProperty("resultSize") int size) {
        this.resultSize = size;
    }

}
