package EZShare.networking;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Create & get a long-live ObjectMapper object.
 * Created by xierch on 2017/3/23.
 */
class ObjectMapperGetter {
    private static ObjectMapper objectMapper;

    synchronized static ObjectMapper get() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            // objectMapper.enableDefaultTyping();
        }
        return objectMapper;
    }
}
