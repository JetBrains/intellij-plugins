// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util;

import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.UserModel;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kir Maximov
 */
public final class CommunicatorStrings {
  private static final Logger LOG = Logger.getLogger(CommunicatorStrings.class);

  @NonNls
  private static final Pattern SPECIAL_CHAR = Pattern.compile("&#(\\d+);");
  @NonNls
  private static final ResourceBundle ourBundle = ResourceBundle.getBundle("IDEtalkMessages");
  public static final String FAILED_TITLE = getMsg("operation_failed.title");
  private static String ourUsername;

  private CommunicatorStrings() {
  }

  public static String getShortName(Class<?> aClass) {
    String name = aClass.getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  public static String getMyUsername() {
    return ourUsername == null ? System.getProperty("user.name") : ourUsername;
  }

  public static void setMyUsername(String username) {
    ourUsername = username;
  }

  public static String getText(String txt, int count) {
    if (count % 10 == 1) {
      return txt;
    }
    return txt + 's';
  }

  public static void appendItems(StringBuffer text, String itemName, int itemsCount) {
    if (itemsCount > 0) {
      text.append(itemsCount);
      text.append(' ');
      appendItemName(text, itemName, itemsCount);
    }
  }

  public static void appendItemName(StringBuffer text, String itemName, int itemsCount) {
    text.append(itemName);
    if (itemsCount > 1) {
      text.append('s');
    }
  }

  public static String toString(Class<?> aClass, Object[] objects) {
    return getShortName(aClass) + Arrays.asList(objects);
  }

  public static String toString(Class<?> aClass, Object object) {
    return toString(aClass, new Object[]{object});
  }

  public static String fixGroup(String group) {
    return com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces(group) ? UserModel.DEFAULT_GROUP : group.trim();
  }

  /* replaces non-ASCII characters with &#xxx; presentation */
  public static String toXMLSafeString(String str) {
    StringBuilder result = new StringBuilder(str.length() * 3);
    for (char aChar : str.toCharArray()) {
      if (aChar < 0x20 || aChar > 0xff) {
        result.append("&#").append((int)aChar).append(';');
      }
      else {
        result.append(aChar);
      }
    }
    return result.toString();
  }

  /* replaces non-ASCII characters with &#xxx; presentation */
  public static String fromXMLSafeString(String str) {
    if (str == null) return "";
    StringBuilder result = new StringBuilder(str.length());

    Matcher matcher = SPECIAL_CHAR.matcher(str);

    while (matcher.find()) {
      char c = '?';
      try {
        c = (char)Integer.valueOf(matcher.group(1)).intValue();
      }
      catch (NumberFormatException e) {
        LOG.info(e, e);
      }
      matcher.appendReplacement(result, new String(new char[]{c}));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  public static String getMsg(String resourceCode, Object... str) {
    return MessageFormat.format(ourBundle.getString(resourceCode), str);
  }

  public static String substring(String s, char untilMe) {
    int idx = s.indexOf(untilMe);
    if (idx == -1) return s;
    return s.substring(0, idx);
  }

  public static String toXML(Throwable e) {
    @NonNls Element element = new Element("exception", Transport.NAMESPACE);
    if (e.getMessage() != null) {
      element.setAttribute("message", e.getMessage());
    }
    StringWriter out = new StringWriter();
    e.printStackTrace(new PrintWriter(out));
    element.setText(out.toString());
    return new XMLOutputter().outputString(element);
  }

  public static boolean containedIn(@Nullable String s, @NotNull String searched) {
    return s != null && s.contains(searched);
  }
}
