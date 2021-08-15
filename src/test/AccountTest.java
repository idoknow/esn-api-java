package test;

import conn.ESNSession;
import util.Debug;

public class AccountTest {
    public static void main(String[] args)throws Exception {
        Debug.debug=true;
        ESNSession session=new ESNSession("39.100.5.139:3003","rock","000112rock.,.",10000,null);
        session.removeAccount("fu",true);
        System.out.println("done.");
    }
}
