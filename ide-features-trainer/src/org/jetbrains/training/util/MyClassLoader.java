package org.jetbrains.training.util;

import java.io.InputStream;

/**
 * Created by karashevich on 31/03/15.
 */
public class MyClassLoader {

    final public static String RESPATH = "/data/";

    public static final MyClassLoader INSTANCE = new MyClassLoader();

    public static MyClassLoader getInstance(){
        return INSTANCE;
    }

    public InputStream getResourceAsStream(String path){
        return this.getClass().getResourceAsStream(RESPATH + path);
    }

    public String getDataPath(){
        return this.getClass().getResource(RESPATH).getPath();
    }

}
