package org.intellij.plugins.markdown;

import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.intellij.plugins.markdown.injection.LanguageGuesser;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;

public class MarkdownInjectionTest extends LightCodeInsightFixtureTestCase {
  public void testFenceWithLang() {
    doTest("```java\n" +
           "{\"foo\":\n" +
           "  <caret>\n" +
           "  bar\n" +
           "}\n" +
           "```", true);
  }

  public void testFenceWithJs() {
    assert JavascriptLanguage.INSTANCE != null;
    assertNotNull(LanguageGuesser.INSTANCE.guessLanguage("js") != null);
  }

  private void doTest(String text, boolean shouldHaveInjection) {
    final PsiFile file = myFixture.configureByText(MarkdownFileType.INSTANCE, text);
    assertEquals(shouldHaveInjection, !file.findElementAt(myFixture.getCaretOffset()).getLanguage().isKindOf(MarkdownLanguage.INSTANCE));
  }
}
