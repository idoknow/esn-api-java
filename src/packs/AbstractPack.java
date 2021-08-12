package packs;

import conn.ESNSession;
import conn.NetPackage;
import crypto.MD5Util;
import util.Debug;

import java.util.Date;

public class AbstractPack {
    protected int code=0;

    public PackResult writeToWaitResult(ESNSession session,boolean crypto)throws Exception{
        Debug.debug("packCode:"+code);
        return new NetPackage(code,this,crypto,session.getRsaKey()).writePackWaitResult(session);
    }
    public void writeTo(ESNSession session,boolean crypto)throws Exception{
        Debug.debug("packcode:"+code);
        new NetPackage(code,this,crypto,session.getRsaKey()).writeTo(session);
    }
    public static String randToken(){
        int randNum=(int)(Math.random()*10000);
        return MD5Util.stringToMD5(new Date().getTime()+":java:"+randNum);
    }
}
