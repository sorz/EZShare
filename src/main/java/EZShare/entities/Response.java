package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For JSON. Server's response to a request command.
 * Either success or error. Error response has a errorMessage.
 * Created by xierch on 2017/3/22.
 */
public class Response {
    private static final String RESPONSE_SUCCESS = "success";
    private static final String RESPONSE_ERROR = "error";

    private String response;
    // Following line tell Jackson to ignore errorMessage on success response.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorMessage;

    public String getResponse() {
        return response;
    }

    private void setResponse(String response) {
        this.response = response;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return getResponse() != null && getResponse().toLowerCase().equals(RESPONSE_SUCCESS);
    }

    public boolean isError() {
        // Unknown response also treated as "normal" error here.
        // Shall we throw a exception instead?
        return !isSuccess();
    }

    @JsonCreator
    public static Response createSuccess() {
        Response response = new Response();
        response.setResponse(RESPONSE_SUCCESS);
        return response;
    }

    @JsonCreator
    public static Response createError(@JsonProperty("errorMessage") String errorMessage) {
        Response response = new Response();
        response.setResponse(RESPONSE_ERROR);
        response.setErrorMessage(errorMessage);
        return response;
    }

}