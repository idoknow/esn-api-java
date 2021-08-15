package test;

import conn.ESNSession;
import conn.ISessionListener;
import packs.PackRespNotification;
import packs.PackResult;
import util.Debug;

public class LoginTest {
    public static void main(String[] args)throws Exception {
        Debug.debug=true;
        ESNSession esnSession=new ESNSession("39.100.5.139:3003", "rock", "000112rock.,.", 10000, new ISessionListener() {
            @Override
            public void notificationReceived(PackRespNotification notification) {
                System.out.println("notifi:"+notification.Title);
            }

            @Override
            public void sessionLogout(PackResult result) {

            }
        });
        esnSession.requestNotifications(0,100);
        System.out.println("request call done.");
        Thread.sleep(10000);
    }
}
