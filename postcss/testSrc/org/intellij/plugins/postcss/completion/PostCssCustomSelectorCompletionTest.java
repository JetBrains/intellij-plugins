package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
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
    assertEquals("--test", myFixture.getCompletionVariants(getTestName(true) + ".pcss").get(0));
  }

  public void testCustomSelectorTwoDefinitions() {
    List<String> variants = myFixture.getCompletionVariants(getTestName(true) + ".pcss");
    assertEquals("--test", variants.get(0));
    assertEquals("--test2", variants.get(1));
  }

  public void testCustomSelectorPriorityWithImport() {
    myFixture.configureByFiles(getTestName(true) + ".pcss", "definition.pcss");
    LookupElement[] lookupElements = myFixture.completeBasic();
    LookupElement first = lookupElements[0];
    LookupElement second = lookupElements[1];

    LookupElementPresentation presentation = new LookupElementPresentation();
    first.renderElement(presentation);
    assertEquals("test", presentation.getItemText());
    assertEquals("definition.pcss:1", presentation.getTypeText());
    second.renderElement(presentation);
    assertEquals("z-in-file", presentation.getItemText());
    assertEquals("customSelectorPriorityWithImport.pcss:3", presentation.getTypeText());
  }

  public void testCustomSelectorPriorityWithoutImport() {
    myFixture.configureByFiles(getTestName(true) + ".pcss", "definition.pcss");
    LookupElement[] lookupElements = myFixture.completeBasic();
    LookupElement first = lookupElements[0];
    LookupElement second = lookupElements[1];

    LookupElementPresentation presentation = new LookupElementPresentation();
    first.renderElement(presentation);
    assertEquals("z-in-file", presentation.getItemText());
    assertEquals("customSelectorPriorityWithoutImport.pcss:1", presentation.getTypeText());
    second.renderElement(presentation);
    assertEquals("test", presentation.getItemText());
    assertEquals("definition.pcss:1", presentation.getTypeText());
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
    List<String> variants = myFixture.getCompletionVariants(getTestName(true) + ".pcss");
    assertFalse(variants.contains("--test"));
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customSelector";
  }

  private void doTest() {
    myFixture.testCompletion(getTestName(true) + ".pcss", getTestName(true) + "_after.pcss");
  }

}
