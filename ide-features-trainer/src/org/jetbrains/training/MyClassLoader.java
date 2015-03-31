package org.jetbrains.training;

import java.io.InputStream;

/**
 * Created by karashevich on 31/03/15.
 */
public class MyClassLoader {

    final public static String RESPATH = "res/";

    public static final MyClassLoader INSTANCE = new MyClassLoader();

    public static MyClassLoader getInstance(){
        return INSTANCE;
    }

    public InputStream getResourceAsStream(String path){
        return this.getClass().getResourceAsStream(RESPATH + path);
    }

}
