package org.angularjs.editor;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.settings.AngularJSConfig;

/**
 * Created by denofevil on 27/11/13.
 */
public class AngularTypedHandlerTest extends LightPlatformCodeInsightFixtureTestCase {
  public void testBracketsClosing() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{<caret>");
    myFixture.type("{");
    myFixture.checkResult("{{<caret>}}");
  }

  public void testBracketsNotClosingTwice() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{<caret>}}");
    myFixture.type("{");
    myFixture.checkResult("{{<caret>}}");
  }

  public void testInserWhitespace() {
    boolean oldWhitespace = AngularJSConfig.getInstance().INSERT_WHITESPACE;
    try {
      AngularJSConfig.getInstance().INSERT_WHITESPACE = true;
      myFixture.configureByText(HtmlFileType.INSTANCE, "{<caret>");
      myFixture.type("{");
      myFixture.checkResult("{{ <caret> }}");
    }
    finally {
      AngularJSConfig.getInstance().INSERT_WHITESPACE = oldWhitespace;
    }
  }
}
