package packs;

public class PackReqRecent extends AbstractPack{
    public int Limit;
    public String Token;
    public PackReqRecent(int limit,String token){
        this.code=10;
        this.Limit=limit;
        this.Token=token;
    }
}
