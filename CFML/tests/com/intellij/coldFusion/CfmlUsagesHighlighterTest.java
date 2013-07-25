package com.intellij.coldFusion;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * @author vnikolaenko
 */
public class CfmlUsagesHighlighterTest extends CfmlCodeInsightFixtureTestCase {
  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  private RangeHighlighter[] getUsages() {
    return myFixture.testHighlightUsages(Util.getInputDataFileName(getTestName(true)));
  }

  public void testFunctionArguments() {
    assertEquals(3, getUsages().length);
  }

  public void testFunctionUsages() {
    assertEquals(3, getUsages().length);
  }

  public void testFunctionUsagesFromString() {
    assertEquals(3, getUsages().length);
  }

  public void testHighlightVariablesByFunctionScope() {
    assertEquals(3 + 1, getUsages().length); // definition counts twice
  }

  public void testFindUsagesFromDefinitionInString() {
    assertEquals(3, getUsages().length);
  }

  public void testFromNamedAttribute() {
    assertEquals(2, getUsages().length);
  }

  public void testHighlightVariableFromDefeinition() {
    assertEquals(2 + 1, getUsages().length); // definition counts twice
  }

  public void testScriptFunctionUsages() {
    assertEquals(4, getUsages().length);
  }

  public void testHighlightFromScopedVariableInComments() {
    assertEquals(3, getUsages().length);
  }

  public void testIncorrectAttributeName() throws Throwable {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.checkHighlighting();
    TargetElementUtilBase.getInstance()
      .findTargetElement(myFixture.getEditor(), TargetElementUtilBase.ELEMENT_NAME_ACCEPTED, myFixture.getCaretOffset());
  }

  public void testIncorrectAttributeNameInArgumentTag() throws Throwable {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.checkHighlighting();
    TargetElementUtilBase.getInstance()
      .findTargetElement(myFixture.getEditor(), TargetElementUtilBase.ELEMENT_NAME_ACCEPTED, myFixture.getCaretOffset());
  }

  @Override
  protected String getBasePath() {
      return "/usagesHighlighter";
  }
}
