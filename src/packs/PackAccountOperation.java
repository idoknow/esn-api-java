package packs;

public class PackAccountOperation extends AbstractPack{
    public String Oper;
    public String Name;
    public String Pass;
    public String Priv;
    public boolean kick;
    public String Token;
    public static final String ADD_ACCOUNT="add",REMOVE_ACCOUNT="remove";
    public static final String ACCOUNT="account",PULL="pull",PUSH="push";
    public PackAccountOperation(String oper,String name,String pass,String priv,boolean kick,String token){
        this.code=7;
        this.Token=token;
        this.Oper=oper;
        this.Name=name;
        this.Pass=pass;
        this.Priv=priv;
        this.kick=kick;
    }
}
