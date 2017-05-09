package EZShare.server.subscription;

import EZShare.entities.Resource;

/**
 * Created on 2017/5/10.
 */
interface RelayService {
    String subscribe(Resource template);
    void unsubscribe(String relayId);
}
