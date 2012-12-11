/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.communicator.idea;

import com.intellij.execution.filters.BrowserHyperlinkInfo;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.StringUtil;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kir
 */

/**
 * @noinspection "Magic number"
 */
public class ConsoleUtil {
  private static final Color[] OUR_COLORS = new Color[]{
    JBColor.BLUE, JBColor.GREEN,
    new Color(128, 0, 128),
    new Color(128, 100, 0),
    new Color(128, 0, 0),
    new Color(0, 128, 0),
    new Color(0, 0, 128),
    new Color(200, 110, 100),
    new Color(200, 150, 100),
    new Color(120, 150, 150),
    new Color(120, 150, 200),
    new Color(75, 100, 130),
    new Color(160, 60, 0),
    new Color(60, 170, 180),
  };
  public static final int CONSOLE_WIDTH = 700;
  public static final int CONSOLE_HEIGHT = 200;

  private static final Pattern URL_PATTERN =
      Pattern.compile("\\w+://[-\\|\\w.]+:*[/\\w-#%?.=&]*[/\\w-#%?=&]");

  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("kk:mm:ss");

  private ConsoleUtil() {
  }

  public static void outputMessage(ConsoleMessage consoleMessage, Project myProject, ConsoleView console) {
    outputMessage(myProject, console, consoleMessage.getUsername(), consoleMessage.getTitle() + ' ', consoleMessage);
  }

  private static void outputMessage(Project project, final ConsoleView console, String userName, final String header, Printer printer) {
    final int contentSize = console.getContentSize();
    console.print(header, getOutputAttributes(userName));

    printer.printMessage(project, console);
    //console.print("\n", ConsoleViewContentType.NORMAL_OUTPUT);

    console.performWhenNoDeferredOutput(new Runnable() {
      public void run() {
        int allContent = console.getContentSize();
        console.scrollTo(Math.min(allContent, contentSize + header.length()));
      }
    });
  }

  private static ConsoleViewContentType getOutputAttributes(String userName) {
    return new ConsoleViewContentType("UserName",
        new TextAttributes(getColor(userName), Color.white, Color.white, null, Font.BOLD)) {
    };
  }

  static Color getColor(String userName) {
    int a = userName.hashCode() * userName.length();
    int idx = Math.abs(a*a) % OUR_COLORS.length;
    return OUR_COLORS[idx];
  }

  public static String getHeader(String title, User user, Date when) {
    return StringUtil.getMsg("console.header", formatDate(when), user.getDisplayName());
  }

  public static String formatDate(Date when) {
    return DATE_FORMAT.format(when);
  }

  public static void printMessageIfExists(ConsoleView console, String message, ConsoleViewContentType textAttributes) {
    message = message.trim();
    if (StringUtil.isNotEmpty(message)) {

      Matcher matcher = URL_PATTERN.matcher(message);
      int pos = 0;
      while (matcher.find(pos)) {

        print(console, message.substring(pos, matcher.start()), textAttributes);
        console.printHyperlink(matcher.group(), new BrowserHyperlinkInfo(matcher.group()));
        pos = matcher.end();
      }
      print(console, message.substring(pos), textAttributes);
      console.print("\n", textAttributes);
    }
  }

  private static void print(ConsoleView console, String s, ConsoleViewContentType textAttributes) {
    if (StringUtil.isNotEmpty(s)) {
      console.print(s, textAttributes);
    }
  }

  public interface Printer {
    void printMessage(Project project, ConsoleView console);
  }

}
