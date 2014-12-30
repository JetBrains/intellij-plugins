package org.jetbrains.training;

import java.io.*;
import java.util.Scanner;

/**
 * Created by karashevich on 17/12/14.
 */
public class TestOneReflection {

    private String fileName;

    public TestOneReflection(String something) {
        this.fileName = something;
    }

    public TestOneReflection()  {
        this.fileName = "yield";
    }

    public String reflectMe() throws IOException {

        InputStream is = this.getClass().getResourceAsStream(fileName);
        return new Scanner(is).useDelimiter("\\Z").next();

    }

}
