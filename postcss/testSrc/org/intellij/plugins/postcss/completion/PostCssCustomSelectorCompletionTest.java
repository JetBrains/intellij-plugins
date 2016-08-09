package org.intellij.plugins.postcss.completion;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorCompletionTest extends PostCssCompletionTest {

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

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customSelector";
  }
}
