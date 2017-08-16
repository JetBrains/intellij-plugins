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

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Kir
 */
public class HardWrapUtilTest extends TestCase {
  private JTextArea myTextArea;
  private HardWrapUtil myWrapper;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myTextArea = new JTextArea();
    myWrapper = new HardWrapUtil(myTextArea);
  }

  public void testInit() {
    assertTrue("initPerProject wrapping", myTextArea.getWrapStyleWord());
    assertTrue("initPerProject wrapping", myTextArea.getLineWrap());

    Font font = myTextArea.getFont();
    FontMetrics fontMetrics = myTextArea.getFontMetrics(font);
    assertEquals("Should be monospaced", fontMetrics.charWidth('m'), fontMetrics.charWidth('i'));

    assertEquals("wrong char width", fontMetrics.charWidth('m'), myWrapper.getCharWidth());
  }

  public void testGetWrappedText() {
    _testWrapping(5, "abc", "abc");
    _testWrapping(5, "abc de", "abc \nde");
    _testWrapping(1, "aaa", "aaa");
    _testWrapping(3, "abc def", "abc \ndef");

    _testWrapping(5, "  aabc", "aabc");
    _testWrapping(5, "aabc  ", "aabc");

    _testWrapping(3, "a\r\n\rbc", "a\n\nbc");

    _testWrapping(3, "a  ddd", "a  \nddd");
    _testWrapping(3, "a d dd", "a d \ndd");

    _testWrapping(3, "abcddd aaa  ffdd", "abcddd\naaa \nffdd");

  }


  public void testExceptionsAreNotWrapped() {
    StringWriter out = new StringWriter();
    new Throwable().printStackTrace(new PrintWriter(out));
    String text = out.toString().replaceAll("\t", "  ").replaceAll("\r\n", "\n").trim();

    _testWrapping(3, text, text);

    _testWrapping(3, "\nmes sage\n" + text, "mes \nsage\n" + text);
  }

  private void _testWrapping(int cols, String text, String expected) {
    myTextArea.setSize(myWrapper.getCharWidth() * cols, 100);
    myTextArea.setText(text);
    assertEquals("Incorrect wrapped text for '" + text + "' " + cols,
        expected,
        myWrapper.getText());
  }
}
