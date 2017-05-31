package org.angularjs.editor;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

/**
 * @author Dennis.Ushakov
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

  public void testBracketsNotBreakingAtEnd() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{<caret>");
    myFixture.type("}");
    myFixture.checkResult("{{}}<caret>");
  }

  public void testClosingBracketsSkipped() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{<caret>}}");
    myFixture.type("}");
    myFixture.checkResult("{{}<caret>}");
  }

  public void testSecondClosingBracket() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{}<caret>");
    myFixture.type("}");
    myFixture.checkResult("{{}}<caret>");
  }

  public void testInsertWhitespace() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{<caret>");
    JSCodeStyleSettings settings = JSCodeStyleSettings.getSettings(myFixture.getFile());
    boolean oldWhitespace = settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS;
    try {
      settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = true;
      myFixture.type("{");
      myFixture.checkResult("{{ <caret> }}");
    }
    finally {
      settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = oldWhitespace;
    }
  }
}
