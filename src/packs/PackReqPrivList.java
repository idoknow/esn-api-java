package packs;

public class PackReqPrivList extends AbstractPack{
    public String Priv;
    public String Token;
    public PackReqPrivList(String token){
        this.code=6;
        this.Token=token;
    }
}
