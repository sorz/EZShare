package EZShare.server;

import EZShare.entities.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * All commands that client may send to server.
 * Created on 2017/3/24.
 */
public interface ClientCommandHandler {
    void doPublish(Publish cmd) throws CommandHandleException;
    void doRemove(Remove cmd) throws CommandHandleException;
    void doShare(Share cmd) throws CommandHandleException;
    int doQuery(Query cmd, Consumer<Void> ok, Consumer<Resource> consumer)
            throws CommandHandleException;
    Pair<Resource, InputStream> doFetch(Fetch cmd) throws CommandHandleException;
    void doExchange(Exchange cmd) throws CommandHandleException;
}
