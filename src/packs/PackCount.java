package packs;

public class PackCount extends AbstractPack{
    int From;
    int To;
    String Token;
    public PackCount(int from,int to,String token){
        this.code=11;
        this.From=from;
        this.To=to;
        this.Token=token;
    }
}
