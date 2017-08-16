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

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import jetbrains.communicator.core.impl.BaseTestCase;
import org.jmock.Mock;

import java.awt.*;

/**
 * @author Kir
 */
public class ConsoleUtilTest extends BaseTestCase {
  private Mock myConsoleMock;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myConsoleMock = mock(ConsoleView.class);

  }

  private ConsoleView console() {
    return (ConsoleView) myConsoleMock.proxy();
  }

  public void testGetColor() {
    Color color = ConsoleUtil.getColor("Alexey.Pegov");
    System.out.println("" + color);
  }

  public void testPrintEmptyMessage() {
    ConsoleUtil.printMessageIfExists(console(), "  ", null);
  }

  public void testPrintNormalMessage() {
    myConsoleMock.expects(once()).method("print").with(eq("message"), eq(ConsoleViewContentType.USER_INPUT));
    myConsoleMock.expects(once()).method("print").with(eq("\n"), eq(ConsoleViewContentType.USER_INPUT));

    ConsoleUtil.printMessageIfExists(console(), "message", ConsoleViewContentType.USER_INPUT);
  }

  public void testPrintMessageWithUrl() {

    myConsoleMock.expects(once()).method("print").with(eq("1 "), eq(null));
    myConsoleMock.expects(once()).method("printHyperlink").with(eq("httP://ww-_w.ru:81"), NOT_NULL);
    myConsoleMock.expects(once()).method("print").with(eq(". 2 "), eq(null));
    myConsoleMock.expects(once()).method("printHyperlink").with(eq("ftp://dd/a.html#?we=32"), NOT_NULL);
    myConsoleMock.expects(once()).method("print").with(eq(", 3"), eq(null));
    myConsoleMock.expects(once()).method("print").with(eq("\n"), eq(null));

    ConsoleUtil.printMessageIfExists(console(),
        "1 httP://ww-_w.ru:81. 2 ftp://dd/a.html#?we=32, 3", null);
  }

  public void testPrintMessageWithUrl1() {

    myConsoleMock.expects(once()).method("printHyperlink").with(eq("httP://ww-_w.ru"), NOT_NULL);
    myConsoleMock.expects(once()).method("print").with(eq("\n"), eq(null));

    ConsoleUtil.printMessageIfExists(console(),
        "httP://ww-_w.ru", null);
  }

  public void testPrintMessageWithUrl2() {

    myConsoleMock.expects(once()).method("printHyperlink").with(eq("http://www.livejournal.com/community/ideabook/19930.html?nc=4&style=mine"), NOT_NULL);
    myConsoleMock.expects(once()).method("print").with(eq("\n"), eq(null));

    ConsoleUtil.printMessageIfExists(console(),
        "http://www.livejournal.com/community/ideabook/19930.html?nc=4&style=mine", null);
  }


}
