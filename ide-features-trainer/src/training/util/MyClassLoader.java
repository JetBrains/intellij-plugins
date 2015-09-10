package training.util;

import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by karashevich on 31/03/15.
 */
public class MyClassLoader {

    final public static String RESPATH = "/data/";
    final public static String IMGPATH = "/img/";

    public static final MyClassLoader INSTANCE = new MyClassLoader();

    public static MyClassLoader getInstance(){
        return INSTANCE;
    }

    public InputStream getResourceAsStream(String path){
        return this.getClass().getResourceAsStream(RESPATH + path);
    }

    @Nullable
    public BufferedImage getImageResourceAsStream(String path){
        InputStream in = this.getClass().getResourceAsStream(IMGPATH + path);
        try {
            return ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDataPath(){
        return this.getClass().getResource(RESPATH).getPath();
    }

}
