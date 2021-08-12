package packs;

public class PackRespNotification extends AbstractPack{
    public int Id;
    public String Target;
    public String Time;
    public String Title;
    public String Content;
    public String Source;
    public String Token;
    public PackRespNotification(String token){
        this.code=5;
        this.Token=token;
    }
}
