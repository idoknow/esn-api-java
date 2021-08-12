package conn;

import com.google.gson.Gson;
import packs.*;
import util.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class ESNSession implements Runnable{
    public static final String version="api-java-0.1";



    private Socket socket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    private int protocolVersion=0;
    String rsaKey="";
    boolean available=false;
    String privilege="";

    private final Boolean timeLock=false;
    private Exception exception;
    private ISessionListener listener;

    private Thread proxyThr;

    public ESNSession(String addr, String user, String pass, long timeout, ISessionListener listener)throws Exception{
        Thread loginThr=new Thread(()->{
            try {
                login(addr,user,pass,listener);
            } catch (Exception e) {
                exception=e;
            }
            synchronized (timeLock){
                timeLock.notify();
            }
        });
        loginThr.start();
        Thread timeThr=new Thread(()->{
            try {
                Thread.sleep(timeout);
                loginThr.stop();
            } catch (InterruptedException e) {
                exception=e;
            }
            synchronized (timeLock){
                timeLock.notify();
            }
        });
        timeThr.start();
        synchronized (timeLock){
            timeLock.wait();
        }
        if (exception!=null){
            throw exception;
        }
    }
    private void login(String addr, String user, String pass, ISessionListener listener)throws Exception{
        this.listener=listener;
        socket=new Socket(addr.split(":")[0],Integer.parseInt(addr.split(":")[1]));
        dataInputStream=new DataInputStream(socket.getInputStream());
        dataOutputStream=new DataOutputStream(socket.getOutputStream());

        dataOutputStream.writeInt(119812525);
        protocolVersion=dataInputStream.readInt();
        Debug.debug("protocolVersion:"+protocolVersion);
        if (protocolVersion>1999){
            throw new Exception("protocol not support:"+protocolVersion);
        }


//        PackResult result=new NetPackage(AbstractPack.PACK_LOGIN,login,false,"").writePack(this);
        PackResult result=new PackLogin(user, pass,AbstractPack.randToken()).writeToWaitResult(this,false);
        Debug.debug("login result:"+result.Result+" err:"+result.Error);
        if (!result.Error.equals("")){
            throw new Exception(result.Error);
        }

        result=new PackReqPrivList(AbstractPack.randToken()).writeToWaitResult(this,false);
        if (!result.Error.equals("")){
            throw new Exception(result.Error);
        }
        this.privilege=(new NetPackage(this,"").getPackObj(PackReqPrivList.class)).Priv;
        Debug.debug("priv:"+privilege);

        available=true;
        this.proxyThr=new Thread(this);
        this.proxyThr.start();
    }

    private HashMap<String,NetPackage> waiterMap=new HashMap<>();

    private HashMap<String,NetPackage> receivedMap=new HashMap<>();

    @Override
    public void run(){
        try {
            while (true) {
                NetPackage netPackage = new NetPackage(this, "");
                Debug.debug("Selector:selectPack:code="+netPackage.code);
                if (netPackage.code==5){
                    if (this.listener!=null){
                        this.listener.notificationReceived(new Gson().fromJson(netPackage.json,PackRespNotification.class));
                    }
                }else {
                    PackToken token = netPackage.getPackObj(PackToken.class);

                    receivedMap.put(token.Token, netPackage);

                    if ("LogoutPackage".equals(token.Token)){
                        if (this.listener!=null){
                            this.listener.sessionLogout(new Gson().fromJson(netPackage.json,PackResult.class));
                        }
                    }else if (waiterMap.containsKey(token.Token)) {
                        synchronized (waiterMap.get(token.Token)) {
                            waiterMap.get(token.Token).notify();
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public <T> T selectPack(String token,Class<T> tClass){
        if (receivedMap.containsKey(token)){
            return new Gson().fromJson(receivedMap.get(token).json,tClass);
        }else {//已收到的包不存在，等待
            NetPackage waitPack=new NetPackage();
            waiterMap.put(token,waitPack);
            synchronized (waiterMap.get(token)){
                try {
                    waitPack.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //收到了
            Debug.debug("Successfully selected a wait pack");
            return new Gson().fromJson(receivedMap.get(token).json,tClass);
        }
    }

    public boolean isAvailable(){
        return available;
    }
    public int getProtocolVersion(){
        return protocolVersion;
    }
    public String getRsaKey(){
        return rsaKey;
    }

    public boolean can(String priv){
        return this.privilege.contains(priv);
    }



    public void requestNotifications(int from,int limit)throws Exception{
        String token=AbstractPack.randToken();
        new PackRequest(from,limit,token).writeTo(this,false);
        PackResult result=selectPack(token,PackResult.class);
        if (!"".equals(result.Error))
            throw new Exception(result.Error);
    }
    public void addAccount(String user,String pass,String privilege)throws Exception{
        String token=AbstractPack.randToken();
        new PackAccountOperation(PackAccountOperation.ADD_ACCOUNT,user,pass,privilege,false,token)
                .writeTo(this,false);
        PackResult result=selectPack(token,PackResult.class);
        if (!"".equals(result.Error))
            throw new Exception(result.Error);
    }
    public void removeAccount(String user,boolean kickAllSessionOfThisUser)throws Exception{
        String token=AbstractPack.randToken();
        new PackAccountOperation(PackAccountOperation.REMOVE_ACCOUNT,user,"","",kickAllSessionOfThisUser,token)
                .writeTo(this,false);
        PackResult result=selectPack(token,PackResult.class);
        if (!"".equals(result.Error))
            throw new Exception(result.Error);
    }

}