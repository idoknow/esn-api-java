package conn;

import com.google.gson.Gson;
import packs.*;
import util.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class ESNSession implements Runnable{
    public static final String version="api-java-0.3";

    private Socket socket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    private int protocolVersion=0;
    String rsaKey="";
    boolean available=false;
    final Boolean avaiLock=false;
    String privilege="";

    private final Boolean timeLock=false;
    private Exception exception;
    private ISessionListener listener;

    private Thread proxyThr;
    boolean alreadyTimeout=false;
    private long timeout=5000;

    boolean failed3times=false;

    private Timer alive=new Timer();

    public ESNSession(String addr, String user, String pass, long timeout, ISessionListener listener)throws Exception{
        this.timeout=timeout;
        Thread loginThr=new Thread(()->{
            try {
                login(addr,user,pass,listener);
                this.exception=null;
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
//                loginThr.stop();
                alreadyTimeout=true;
            } catch (InterruptedException e) {
                exception=e;
            }
            if (!available)
                this.exception=new TimeoutException("method time out.");
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
    public synchronized boolean reConnect(String addr, String user, String pass)throws Exception{
        this.available=false;
        this.alreadyTimeout=false;
        try{
            this.socket.close();
        }catch (Exception ignored){}
        Thread loginThr=new Thread(()->{
            try {
                login(addr,user,pass,listener);
                this.exception=null;
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
//                loginThr.stop();
                alreadyTimeout=true;
            } catch (InterruptedException e) {
                exception=e;
            }
            if (!available)
                this.exception=new TimeoutException("method time out.");
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
        return available;
    }
    private void login(String addr, String user, String pass, ISessionListener listener)throws Exception{
        synchronized (avaiLock) {
            this.listener = listener;
            String host = addr.split(":")[0];
            int port = Integer.parseInt(addr.split(":")[1]);
            Debug.debug("Dial:host:" + host + " port:" + port);
            socket = new Socket(host, port);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            dataOutputStream.writeInt(119812525);
            protocolVersion = dataInputStream.readInt();
            Debug.debug("protocolVersion:" + protocolVersion);
            if (protocolVersion > 1999) {
                throw new Exception("protocol not support:" + protocolVersion);
            }


//        PackResult result=new NetPackage(AbstractPack.PACK_LOGIN,login,false,"").writePack(this);
            PackResult result = new PackLogin(user, pass, AbstractPack.randToken()).writeToWaitResult(this, false);
            Debug.debug("login result:" + result.Result + " err:" + result.Error);
            if (!result.Error.equals("")) {
                throw new Exception(result.Error);
            }

            result = new PackReqPrivList(AbstractPack.randToken()).writeToWaitResult(this, false);
            if (!result.Error.equals("")) {
                throw new Exception(result.Error);
            }
            this.privilege = (new NetPackage(this, "").getPackObj(PackReqPrivList.class)).Priv;
            Debug.debug("priv:" + privilege);

            if (alreadyTimeout)
                return;

            available = true;

            Debug.debug("Available=" + available);
            this.proxyThr = new Thread(this);
            this.proxyThr.start();
            this.alive.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        isConnected();
                    } catch (Exception ignored) {
                    }
                }
            }, new Date(), 300000);
        }
    }
    public void dispose(){
        this.available=false;
        try {
            this.alive.cancel();
            this.proxyThr.stop();
        }catch (Exception ignored){}
        try {
            this.socket.close();
        }catch (Exception ignored){}
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
        this.available=false;
        if (this.listener!=null){
            PackResult result=new PackResult();
            result.Error="connection closed";
            result.Result="";
            this.listener.sessionLogout(result);
        }
    }

    public <T> T selectPack(String token,Class<T> tClass){
        if (!receivedMap.containsKey(token)) {//已收到的包不存在，等待
            NetPackage waitPack = new NetPackage();
            waiterMap.put(token, waitPack);
            synchronized (waiterMap.get(token)) {
                try {
                    waitPack.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //收到了
            Debug.debug("Successfully selected a wait pack");
        }
        T result1=new Gson().fromJson(receivedMap.get(token).json,tClass);
        receivedMap.remove(token);
        waiterMap.remove(token);
        return result1;
    }

    private synchronized boolean isConnected()throws Exception{
        Debug.debug("Checking state");
        Boolean waiter=false;
        boolean succ=false;
        this.exception=null;
        Thread requestThr=new Thread(()->{
            try {
                String token = AbstractPack.randToken();
                new PackTest(token).writeTo(this, false);
                PackResult testResult = selectPack(token,PackResult.class);
                synchronized (waiter){
                    waiter.notify();
                }
            }catch (Exception e){
                exception=e;
            }
        });
        Thread countDown=new Thread(()->{
            try{
                Thread.sleep(timeout);
                exception=new Exception("test timeout");
            }catch (Exception e){
                exception=e;
            }
            synchronized (waiter){
                waiter.notify();
            }
        });
        requestThr.start();
        countDown.start();
        synchronized (waiter){
            waiter.wait();
        }
//        System.out.println("Check state:"+(exception==null)+"  available="+available+" "+isAvailable());
        return exception==null;
    }

    public boolean isAvailable(){
        synchronized (avaiLock) {
            return available;
        }
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


    public void pushNotification(String target,String title,String content)throws Exception{
        if (!isAvailable()){
            throw new Exception("session unavailable");
        }
        String token=AbstractPack.randToken();
        new PackPush(target,title,content,token).writeTo(this,false);
        PackResult result=selectPack(token,PackResult.class);
        if (!"".equals(result.Error))
            throw new Exception(result.Error);
    }


    public int countNotifications(int from,int to)throws Exception{
        if (!isAvailable()){
            throw new Exception("session unavailable");
        }
        String token=AbstractPack.randToken();
        new PackCount(from,to,token).writeTo(this,false);
        PackResult result=selectPack(token,PackResult.class);
        if (!"".equals(result.Error)){
            throw new Exception(result.Error);
        }
        PackRespCount count=selectPack(token,PackRespCount.class);
        return count.Amount;
    }

    @Deprecated
    public void requestNotifications(int from,int limit)throws Exception{
        requestNotifications(from,0,limit);
    }
    public void requestNotifications(int from,int to,int limit)throws Exception{
        if (!isAvailable()){
            throw new Exception("session unavailable");
        }
        String token=AbstractPack.randToken();
        new PackRequest(from,to,limit,token).writeTo(this,false);
        PackResult result=selectPack(token,PackResult.class);
        if (!"".equals(result.Error)) {
            throw new Exception(result.Error);
        }
    }
    public void requestRecent(int limit)throws Exception{
        if (!isAvailable()){
            throw new Exception("session unavailable");
        }
        String token=AbstractPack.randToken();
        new PackReqRecent(limit,token).writeTo(this,false);
        PackResult result=selectPack(token,PackResult.class);
        if (!"".equals(result.Error)){
            throw new Exception(result.Error);
        }
    }
    public void addAccount(String user,String pass,String privilege)throws Exception{
        if (!isAvailable()){
            throw new Exception("session unavailable");
        }
        String token=AbstractPack.randToken();
        new PackAccountOperation(PackAccountOperation.ADD_ACCOUNT,user,pass,privilege,false,token)
                .writeTo(this,false);
        PackResult result=selectPack(token,PackResult.class);
        if (!"".equals(result.Error)) {
            throw new Exception(result.Error);
        }
    }
    public void removeAccount(String user,boolean kickAllSessionOfThisUser)throws Exception{
        if (!isAvailable()){
            throw new Exception("session unavailable");
        }
        String token=AbstractPack.randToken();
        new PackAccountOperation(PackAccountOperation.REMOVE_ACCOUNT,user,"","",kickAllSessionOfThisUser,token)
                .writeTo(this,false);
        PackResult result=selectPack(token,PackResult.class);
        if (!"".equals(result.Error)) {
            throw new Exception(result.Error);
        }
    }

}
