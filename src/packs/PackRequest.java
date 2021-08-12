package packs;

public class PackRequest extends AbstractPack{
    int From=0;
    int Limit=0;
    public String Token;
    public PackRequest(int from,int limit,String token){
        this.code=4;
        this.From=from;
        this.Limit=limit;
        this.Token=token;
    }
}
