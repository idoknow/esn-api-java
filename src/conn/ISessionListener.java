package conn;

import packs.PackRespNotification;
import packs.PackResult;

public interface ISessionListener {
    void notificationReceived(PackRespNotification notification);
    void sessionLogout(PackResult result);
}
