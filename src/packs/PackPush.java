package packs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PackPush extends AbstractPack{
    public String Target;
    String Time;
    public String Title;
    public String Content;
    public String Token;
    private static final SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd','HH:mm:ss z");
    public PackPush(String target,String title,String content,String token){
        this.code=3;
        this.Target=target;
        this.Content=content;
        this.Token=token;
        this.Title=title;
        this.Time=formatter.format(new Date(System.currentTimeMillis()));
    }
}
