package org.intellij.plugins.markdown.editor;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import org.jetbrains.annotations.NotNull;

public class MarkdownQuoteHandlerTest extends LightCodeInsightFixtureTestCase {
  private void doTest(@NotNull String text, char charToType, @NotNull String expectedResult) {
    final PsiFile file = myFixture.configureByText("test.md", text);
    assertInstanceOf(file, MarkdownFile.class);
    
    myFixture.type(charToType);
    myFixture.checkResult(expectedResult);
  }
  
  public void testSingleQuote() {
    doTest("Hello <caret> world", '\'', "Hello '<caret>' world");
  }

  public void testSingleQuoteBeforeWord() {
    doTest("Hello <caret>world", '\'', "Hello '<caret>world");
  }

  public void testSingleQuoteAtEof() {
    doTest("Hello <caret>", '\'', "Hello '<caret>'");
  }

  public void testDoubleQuote() {
    doTest("Hello <caret> world", '"', "Hello \"<caret>\" world");
  }

  public void testBacktick() {
    doTest("Hello <caret> world", '`', "Hello `<caret>` world");
  }

  public void testEmphAsterisk() {
    doTest("Hello <caret> world", '*', "Hello *<caret> world");
  }

  public void testEmphUnderscore() {
    doTest("Hello <caret> world", '_', "Hello _<caret> world");
  }

  public void testSingleQuoteAsApostrophe() {
    doTest("Hello dear<caret> world", '\'', "Hello dear\'<caret> world");
  }

  public void testBacktickAsAccent() {
    doTest("Hello dear<caret> world", '`', "Hello dear`<caret> world");
  }
  
  public void testClosingQuote() {
    doTest("Hello '<caret>' world", '\'', "Hello ''<caret> world");
  }

  public void testClosingQuoteWithWord() {
    doTest("Hello 'cool<caret>' world", '\'', "Hello 'cool'<caret> world");
  }
  
  public void testBacktickShouldBeAdded() {
    doTest("Hello `<caret>` world", '`', "Hello ``<caret>` world");
  }

  public void testBackticksGoodBalance() {
    doTest("Hello ``code<caret>`` world", '`', "Hello ``code`<caret>` world");
  }

  public void testBackticksGoodBalance2() {
    doTest("Hello ``code`<caret>` world", '`', "Hello ``code``<caret> world");
  }

  public void testBackticksBadBalance() {
    doTest("Hello ``code<caret>` world", '`', "Hello ``code`<caret>` world");
  }
}
