package conn;

import com.google.gson.Gson;
import crypto.RSAUtil;
import packs.AbstractPack;
import packs.PackResult;
import util.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class NetPackage {
    int size;
    int code;
    boolean crypto;
    byte[] data;//may be encrypted
    String json;//plain
    String rsaKey;

    public NetPackage(int code, AbstractPack pack,boolean crypto,String rsaKey)throws Exception{
        this.code=code;
        this.crypto=crypto;
        this.rsaKey=rsaKey;
        //TODO parse json
        this.json=new Gson().toJson(pack);
        if (!crypto){
            data=json.getBytes(StandardCharsets.UTF_8);
        }else {
            throw new Exception("encryption not support.");
        }

        this.size= data.length;
    }
    public NetPackage(){
        //void package
    }
    public NetPackage(ESNSession session,String rsaKey)throws Exception{
        this.code=session.dataInputStream.readInt();
        this.size=session.dataInputStream.readInt();
        this.crypto= session.dataInputStream.readInt() == 1;
        this.data=new byte[size];
        session.dataInputStream.read(this.data,0,size);
        if (crypto) {
            data = RSAUtil.decryptByPublicKey(data, rsaKey.getBytes(StandardCharsets.UTF_8));
        }
        this.json=new String(this.data,StandardCharsets.UTF_8);
        Debug.debug("    json:"+this.json);
    }
    public <T> T getPackObj(Class<T> tClass){
        return new Gson().fromJson(json,tClass);
    }


    public static HashMap<Long,String> writePackExceptions=new HashMap<>();


    public PackResult writePackWaitResult(ESNSession session)throws Exception{

        Debug.debug("writing pack wait result:"+json);

        session.dataOutputStream.writeInt(code);
        session.dataOutputStream.writeInt(size);
        session.dataOutputStream.writeInt(crypto?1:0);
        session.dataOutputStream.write(data);
        session.dataOutputStream.flush();
        return new NetPackage(session,"").getPackObj(PackResult.class);
    }
    public void writeTo(ESNSession session)throws Exception{
        Debug.debug("writing pack:"+json);
        session.dataOutputStream.writeInt(code);
        session.dataOutputStream.writeInt(size);
        session.dataOutputStream.writeInt(crypto?1:0);
        session.dataOutputStream.write(data);
        session.dataOutputStream.flush();
    }
    private static long UID_INDEX=0;
    public static synchronized long netPackUID(){
        return UID_INDEX++;
    }
}
