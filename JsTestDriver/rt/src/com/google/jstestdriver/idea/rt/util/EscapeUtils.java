package com.google.jstestdriver.idea.rt.util;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class EscapeUtils {

  private static final char ESCAPE_CHAR = '\\';

  private EscapeUtils() {}

  @NotNull
  public static List<String> split(@NotNull String str, char delimiterChar) {
    Splitter splitter = new Splitter(str, delimiterChar);
    List<String> out = Lists.newArrayList();
    while (splitter.hasNext()) {
      out.add(splitter.next());
    }
    return out;
  }

  @NotNull
  public static String join(@NotNull Collection<String> list, char delimiterChar) {
    if (list.isEmpty()) {
      return "";
    }
    int expectedSize = calcExpectedJoinedSize(list);
    StringBuilder out = new StringBuilder(expectedSize);
    boolean addDelimiter = false;
    for (String str : list) {
      if (addDelimiter) {
        out.append(delimiterChar);
      }
      addDelimiter = true;
      for (int i = 0; i < str.length(); i++) {
        char ch = str.charAt(i);
        if (ch == delimiterChar) {
          out.append(ESCAPE_CHAR).append(delimiterChar);
        } else if (ch == ESCAPE_CHAR) {
          out.append(ESCAPE_CHAR).append(ESCAPE_CHAR);
        } else {
          out.append(ch);
        }
      }
    }
    return out.toString();
  }

  private static int calcExpectedJoinedSize(@NotNull Collection<String> list) {
    int size = list.size() - 1;
    for (String s : list) {
      size += s.length();
    }
    return size;
  }

  private static class Splitter {

    private final String myStr;
    private final char myDelimiterChar;
    private int myInd;
    private final StringBuilder myBuffer = new StringBuilder();
    private boolean myExtraEmptyString = false;

    Splitter(@NotNull String str, char delimiterChar) {
      myStr = str;
      myDelimiterChar = delimiterChar;
      myInd = 0;
    }

    public boolean hasNext() {
      return myExtraEmptyString || myInd < myStr.length();
    }

    public String next() {
      if (myExtraEmptyString) {
        myExtraEmptyString = false;
        return "";
      }
      myBuffer.setLength(0);
      while (myInd < myStr.length()) {
        char ch = myStr.charAt(myInd);
        if (ch == myDelimiterChar) {
          myInd++;
          myExtraEmptyString = myInd == myStr.length();
          return myBuffer.toString();
        }
        if (ch == ESCAPE_CHAR) {
          if (myInd + 1 >= myStr.length()) {
            throw new RuntimeException("String ends with escape char: " + myStr);
          }
          char nextChar = myStr.charAt(myInd + 1);
          if (nextChar == ESCAPE_CHAR || nextChar == myDelimiterChar) {
            myBuffer.append(nextChar);
            myInd += 2;
          } else {
            throw new RuntimeException("Unexpected char is escaped '" + nextChar + "' in " + myStr);
          }
        } else {
          myBuffer.append(ch);
          myInd++;
        }
      }
      return myBuffer.toString();
    }

  }

}
