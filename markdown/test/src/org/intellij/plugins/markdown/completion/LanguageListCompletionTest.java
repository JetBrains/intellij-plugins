package org.intellij.plugins.markdown.completion;

import com.intellij.codeInsight.completion.CompletionAutoPopupTestCase;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.fileTypes.PlainTextParserDefinition;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.intellij.plugins.markdown.MarkdownTestingUtil;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

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

  public static class AutopopupTest extends CompletionAutoPopupTestCase {

    @Override
    protected void setUp() {
      super.setUp();
      assert JavascriptLanguage.INSTANCE != null;
      // Because injector handles the code in the fence and gets parser definition for that lang
      LanguageParserDefinitions.INSTANCE.addExplicitExtension(JavascriptLanguage.INSTANCE, new PlainTextParserDefinition());
    }

    public void testAutopopup() {
      myFixture.configureByText(MarkdownFileType.INSTANCE, "");
      type("```");
      assertNotNull("Lookup should auto-activate", getLookup());
      myFixture.checkResult("```<caret>```");
      assertContainsElements(getLookup().getItems().stream().map(LookupElement::getLookupString).collect(Collectors.toList()),
                             "js", "javascript");
    }
  }

}
