package test;

import conn.ESNSession;
import util.Debug;

public class AccountTest {
    public static void main(String[] args)throws Exception {
        Debug.debug=true;
        ESNSession session=new ESNSession("127.0.0.1:3003","fuckyou","fuckyou",10000,null);
        session.addAccount("fu","fu","pull");
    }
}
