package org.intellij.plugins.markdown.completion;

import com.intellij.idea.Bombed;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.fileTypes.PlainTextParserDefinition;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.intellij.plugins.markdown.MarkdownTestingUtil;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

@SuppressWarnings("Duplicates")
public class LanguageListCompletionTest extends LightPlatformCodeInsightFixtureTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return MarkdownTestingUtil.TEST_DATA_PATH + "/completion/languageList/";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    assert JavascriptLanguage.INSTANCE != null;
    // Because injector handles the code in the fence and gets parser definition for that lang
    LanguageParserDefinitions.INSTANCE.addExplicitExtension(JavascriptLanguage.INSTANCE, new PlainTextParserDefinition());
  }

  private void doTest(@NotNull String toType) {
    myFixture.testCompletionTyping(getTestName(true) + ".md", toType, getTestName(true) + "_after.md");
  }

  private void configure() {
    myFixture.configureByFile(getTestName(true) + ".md");
  }

  private void checkResult() {
    myFixture.checkResultByFile(getTestName(true) + "_after.md");
  }

  private void checkEmptyCompletion() {
    myFixture.testCompletionVariants(getTestName(true) + ".md");
  }

  public void testExistingFence() {
    doTest("javas\n");
  }

  public void testExistingFenceTab() {
    configure();
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "js", "javascript");
    myFixture.type("s\t");
    checkResult();
  }

  public void testExistingNotFence() {
    checkEmptyCompletion();
  }

  public void testInSixQuotes() {
    doTest("javas\n");
  }

  public void testInSixQuotesNotMiddle() {
    checkEmptyCompletion();
  }

  public void testInSixQuotesTab() {
    configure();
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "js", "javascript");
    myFixture.type("s\t");
    checkResult();
  }

  @Bombed(user = "valich", month = Calendar.SEPTEMBER, day = 10)
  public void testAutopopup() {
    myFixture.configureByText(MarkdownFileType.INSTANCE, "");
    myFixture.type("```");
    myFixture.checkResult("```<caret>```");
    assertNotNull("Lookup should auto-activate", myFixture.getLookup());
    assertContainsElements(myFixture.getLookupElementStrings(), "js", "javascript");
  }
}
