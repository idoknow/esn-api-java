package test;

import conn.ESNSession;
import conn.ISessionListener;
import packs.PackRespNotification;
import packs.PackResult;
import util.Debug;

public class CountTest {
    public static void main(String[] args)throws Exception {
        Debug.debug=true;
        ESNSession session=new ESNSession("localhost:3003", "root", "changeMe", 5000, new ISessionListener() {
            @Override
            public void notificationReceived(PackRespNotification notification) {
                System.out.println("new no:"+notification.Content);
            }

            @Override
            public void sessionLogout(PackResult result) {

            }
        });
        for (int i=0;i<100;i++) {
            int count = session.countNotifications(0, 100);
            System.out.println(i+"$$$$$$$$$$$$$$$$count:" + count);
        }
    }
}
