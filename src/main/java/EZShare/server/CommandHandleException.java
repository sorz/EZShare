package EZShare.server;

/**
 * Server cannot handle this command.
 * Created by xierch on 2017/3/24.
 */
public class CommandHandleException extends Exception {
    public CommandHandleException(String message) {
        super(message);
    }
}
