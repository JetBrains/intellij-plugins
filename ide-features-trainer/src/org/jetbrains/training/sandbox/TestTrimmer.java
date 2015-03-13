package org.jetbrains.training.sandbox;

import com.intellij.openapi.util.text.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karashevich on 13/03/15.
 */
public class TestTrimmer {



    public static void main(String[] args) {

        String input = "      " +
                "public class CommentLineDemo {\n" +
                "\n" +
                "    public int fib(int n) {\n" +
                "        int a = 1;\n" +
                "        int b = 1;\n" +
                "\n" +
                "        int tmp;\n" +
                "\n" +
                "        if (n &lt; 2) return 1;\n" +
                "\n" +
                "        for (int i = 0; i &lt; (n - 1); i++) {\n" +
                "            tmp = b;\n" +
                "            b = a;\n" +
                "            a = a + tmp;\n" +
                "        }\n" +
                "\n" +
                "        return a;\n" +
                "    }\n" +
                "}";
        ArrayList<String> ls = (ArrayList<String>) computeTrimmedLines(input);

        for (String l : ls) {
            System.out.print(l + ".");
        }

    }

    private static List<String> computeTrimmedLines(String s) {
        ArrayList<String> ls = new ArrayList<String>();

        for (String it : StringUtil.splitByLines(s) ) {
            String[] splitted = it.split("[ ]+");
            if (splitted != null) {
                for(String element: splitted)
                    if (!element.equals("")) {
                        ls.add(element);
                    }
            }
        }
        return ls;
    }
}
