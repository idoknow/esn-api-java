package test;

import conn.ESNSession;
import conn.ISessionListener;
import packs.PackRespNotification;
import packs.PackResult;
import util.Debug;

public class PushTest {
    public static void main(String[] args) throws Exception{
//        Debug.debug=true;
        ESNSession session=new ESNSession("localhost:3003", "root", "changeMe", 5000, new ISessionListener() {
            @Override
            public void notificationReceived(PackRespNotification notification) {
                System.out.println("new no:"+notification.Content);
            }

            @Override
            public void sessionLogout(PackResult result) {

            }
        });
        session.pushNotification("root,rockchin","JavaPush","TheFirstNotificationPushedByJava");
    }
}
