package packs;

public class PackLogin extends AbstractPack{
    String User;
    String Pass;
    public String Token;
    public PackLogin(String user,String pass,String token){
        this.code=1;
        this.User=user;
        this.Pass=pass;
        this.Token=token;
    }
}
