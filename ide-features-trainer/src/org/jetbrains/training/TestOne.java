package org.jetbrains.training;

import com.intellij.openapi.util.text.StringUtil;
import org.jdom.JDOMException;

import javax.media.*;
import javax.media.format.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by karashevich on 17/12/14.
 */
public class TestOne {

    public static void main(String[] args) throws IOException, JDOMException {

//        final String fileName = "./src";

//        File file = new File(fileName);

//        final String content = new Scanner(file).useDelimiter("\\Z").next();

//        TestOneReflection testOneReflection = new TestOneReflection();
//        testOneReflection.reflectMe();

//        TestOneReflection testOneReflection1 = new TestOneReflection("JavaLessonExample.java");
//        TestOneReflection testOneReflection2 = new TestOneReflection("JavaLessonExample2.java");

//        if (isTaskSolved(testOneReflection1.reflectMe(),testOneReflection2.reflectMe())){
//            System.out.println("YEAH!");
//        }


        Scenario scn = new Scenario("SampleScenario.xml");
        scn.defineRoot();
        scn.printScenario();


    }

    public static boolean isTaskSolved(String target, String current){
        List<String> expected = computeTrimmedLines(target);
        List<String> actual = computeTrimmedLines(current);

        return (expected.equals(actual));
    }

    private static List<String> computeTrimmedLines(String s) {
        ArrayList<String> ls = new ArrayList<String>();

        for (String it : StringUtil.splitByLines(s) ) {
            it.trim();
            ls.add(it);
        }

        return ls;
    }
}
