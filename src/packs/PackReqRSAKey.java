package packs;

public class PackReqRSAKey extends AbstractPack{
    public String Token;
    public PackReqRSAKey(String token){
        this.code=8;
        this.Token=token;
    }
}
