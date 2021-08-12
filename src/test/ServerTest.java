package test;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTest {
    public static void main(String[] args)throws Exception {
        ServerSocket socket=new ServerSocket(1234);
        Socket socket1= socket.accept();
        DataInputStream dataInputStream=new DataInputStream(socket1.getInputStream());
        while (true){
            System.out.println(dataInputStream.readByte());
        }
    }
}
//go 7 36 49 -83
//java