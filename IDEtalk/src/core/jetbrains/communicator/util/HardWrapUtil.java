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

package jetbrains.communicator.util;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir
 */
public class HardWrapUtil {
  private JTextComponent myTextArea;
  private static final Font FONT = new Font("Monospaced", Font.PLAIN, 12);

  public HardWrapUtil(JTextComponent textArea) {
    myTextArea = textArea;

    if (textArea instanceof JTextArea) {
      JTextArea jTextArea = (JTextArea) textArea;
      jTextArea.setLineWrap(true);
      jTextArea.setWrapStyleWord(true);
    }

    textArea.setFont(FONT);
  }

  public int getCharWidth() {
    FontMetrics fontMetrics = myTextArea.getFontMetrics(myTextArea.getFont());
    return fontMetrics.charWidth('m');
  }

  public String getText() {
    String text = myTextArea.getText().trim().replaceAll("\r\n", "\n").replace('\r', '\n');

    StacktraceExtractor extractor = new StacktraceExtractor(text);
    if (extractor.containsStacktrace()) {
      text = extractor.getMessageText();
      return hardWrapText(text) + extractor.getStacktrace();
    }

    return hardWrapText(text);
  }

  private String hardWrapText(final String text) {
    int cols = myTextArea.getWidth() / getCharWidth();
    List<String> result = new ArrayList<String>();
    String[] lines = text.split("\n");
    for (String line : lines) {
      if (line.length() > cols) {
        split(line, cols, result);
      } else {
        result.add(line);
      }
    }

    String res = StringUtil.join(result, '\n');
    if (text.startsWith("\n")) {
      res = "\n" + res;
    }
    if (text.endsWith("\n")) {
      res = res + "\n";
    }
    return res;
  }

  private void split(String line, int cols, List<String> result) {
    if (line.length() <= cols) {
      result.add(line);
      return;
    }

    int i = findWhitespaceBackward(cols, line, result);

    if (i < 0) {
      i = findWhitespaceForward(cols, line, result);
      if (i == line.length()) {
        result.add(line);
      }
    }

  }

  private int findWhitespaceForward(int cols, String line, List<String> result) {
    int i;
    for (i = cols; i < line.length(); i ++) {
      char c = line.charAt(i);
      if (Character.isWhitespace(c)) {
        result.add(line.substring(0, i));
        split(line.substring(i + 1).trim(), cols, result);
        break;
      }
    }
    return i;
  }

  private int findWhitespaceBackward(int cols, String line, List<String> result) {
    int i;
    for (i = cols; i >= 0; i --) {
      char c = line.charAt(i);
      if (Character.isWhitespace(c)) {
        result.add(line.substring(0, i + 1));
        split(line.substring(i + 1).trim(), cols, result);
        break;
      }
    }
    return i;
  }
}
