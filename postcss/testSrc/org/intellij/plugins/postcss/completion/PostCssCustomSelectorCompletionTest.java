package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.util.Pair;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PostCssCustomSelectorCompletionTest extends PostCssFixtureTestCase {

  public void testCustomSelectorTopLevel() {
    doTest();
  }

  public void testCustomSelectorInsideRuleset() {
    doTest();
  }

  public void testCustomSelectorInsideAtRule() {
    doTest();
  }

  public void testCustomSelectorInsideNest() {
    doTest();
  }

  public void testSpaceAfterCaret() {
    doTest();
  }

  public void testSemicolonAfterCaret() {
    doTest();
  }

  public void testSpaceAndSemicolonAfterCaret() {
    doTest();
  }

  public void testSemicolonWithWhitespacesAfterCaret() {
    doTest();
  }

  public void testCustomSelectorOneDefinition() {
    doTest();
  }

  public void testCustomSelectorOneDefinitionIsFirstVariant() {
    doTestPreferred("--test");
  }

  public void testCustomSelectorTwoDefinitions() {
    doTestPreferred("--test", "--test2");
  }

  public void testCustomSelectorPriorityWithImport() {
    myFixture.configureByFile("definition.pcss");
    doTestPreferred(Pair.create("test", "definition.pcss:1"), Pair.create("z-in-file", "customSelectorPriorityWithImport.pcss:3"));
  }

  public void testCustomSelectorPriorityWithoutImport() {
    myFixture.configureByFile("definition.pcss");
    doTestPreferred(Pair.create("z-in-file", "customSelectorPriorityWithoutImport.pcss:1"), Pair.create("test", "definition.pcss:1"));
  }

  public void testCustomSelectorWithImport() {
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testCustomSelectorWithPartialImport() {
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testCustomSelectorWithoutImport() {
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testCustomSelectorInInline() {
    myFixture.testCompletion(getTestName(true) + ".html", getTestName(true) + "_after.html");
  }

  public void testTwoColons() {
    assertFalse(myFixture.getCompletionVariants(getTestName(true) + ".pcss").contains("--test"));
  }

  private void doTestPreferred(String... items) {
    myFixture.configureByFile(getTestName(true) + ".pcss");
    myFixture.completeBasic();
    myFixture.assertPreferredCompletionItems(0, items);
  }

  private void doTestPreferred(Pair<String, String>... items) {
    myFixture.configureByFile(getTestName(true) + ".pcss");
    LookupElement[] lookupElements = myFixture.completeBasic();
    LookupElementPresentation presentation = new LookupElementPresentation();
    for (int i = 0; i < items.length; i++) {
      lookupElements[i].renderElement(presentation);
      assertEquals(items[i].first, presentation.getItemText());
      assertEquals(items[i].second, presentation.getTypeText());
    }
  }

  private void doTest() {
    myFixture.testCompletion(getTestName(true) + ".pcss", getTestName(true) + "_after.pcss");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customSelector";
  }
}
