/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.util;

public class XmlUtil {

    public static String removeHtmlTags(String text) {
        final int n = text.length();
        if (n > 12 && text.substring(0, 6).equals("<html>") && (text.substring(n-7, n).equals("</html>")))
            return text.substring(6, n - 7);
        else
            return text;
    }

    static String addHtmlTags(String text){
        final int n = text.length();
        if (n > 12 && text.substring(0, 6).equals("<html>") && (text.substring(n-7, n).equals("</html>")))
            return text;
        else
            return "<html>" + text + "</html>";
    }


}
