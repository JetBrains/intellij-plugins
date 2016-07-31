package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

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
    myFixture.configureByFiles(getTestName(true) + ".pcss");
    assertEquals("--test", myFixture.completeBasic()[0].getLookupString());
  }

  public void testCustomSelectorTwoDefinitions() {
    myFixture.configureByFiles(getTestName(true) + ".pcss");
    assertEquals("--test", myFixture.completeBasic()[0].getLookupString());
    assertEquals("--test2", myFixture.completeBasic()[1].getLookupString());
  }

  public void testCustomSelectorTwoDefinitionsWithImport() {
    myFixture.configureByFiles("definition.pcss");
    myFixture.configureByFiles(getTestName(true) + ".pcss");
    LookupElement first = myFixture.completeBasic()[0];
    LookupElement second = myFixture.completeBasic()[1];

    LookupElementPresentation presentation = new LookupElementPresentation();
    first.renderElement(presentation);
    assertEquals("z-in-file", presentation.getItemText());
    assertEquals("customSelectorTwoDefinitionsWithImport.pcss:3", presentation.getTypeText());
    second.renderElement(presentation);
    assertEquals("test", presentation.getItemText());
    assertEquals("definition.pcss:1", presentation.getTypeText());
  }

  public void testCustomSelectorWithImport() {
    myFixture.configureByFiles("definition.pcss");
    doTest();
  }

  public void testCustomSelectorWithIncorrectImport() {
    myFixture.configureByFiles("definition.pcss");
    doTest();
  }

  public void testCustomSelectorWithoutImport() {
    myFixture.configureByFiles("definition.pcss");
    doTest();
  }

  public void testCustomSelectorInInline() {
    myFixture.testCompletion(getTestName(true) + ".html", getTestName(true) + "_after.html");
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
