package org.intellij.plugins.postcss.parser;

public class PostCssParsingNestingTest extends PostCssParsingTest {
  public PostCssParsingNestingTest() {
    super("nesting");
  }

  public void testNestingAmpersand() {
    doTest();
  }
  public void testNestingNestRule() {
    doTest();
  }
  public void testSemicolonsCorrect() {
    doTest();
  }
  public void testSemicolonsIncorrect() {
    doTest();
  }
  public void testDirectNestingIncorrect() {
    doTest();
  }
  public void testTopLevelRulesetNesting() {
    doTest();
  }
  public void testNestRulesetInsideAtRule() {
    doTest();
  }
  public void testPartOfNestKeywordTopLevel() {
    doTest();
  }
  public void testPartOfNestKeywordInsideRuleset() {
    doTest();
  }
  public void testPartOfNestKeywordInsideAtRule() {
    doTest();
  }
  public void testPartOfNestKeywordInsidePageAtRule() {
    doTest();
  }
  public void testPartOfNestInsideApplyFunction() {
    doTest();
  }
  public void testNestAtRuleIncorrectSelectorList() {
    doTest();
  }
  public void testAmpersandInSimpleSelector() {
    doTest();
  }
  public void testAmpersandInClass() {
    doTest();
  }
  public void testAmpersandIdSelector() {
    doTest();
  }
  public void testAmpersandInPseudoClasses() {
    doTest();
  }
  public void testAmpersandInPseudoFunction() {
    doTest();
  }
  public void testAmpersandInAttributes() {
    doTest();
  }
  public void testAmpersandWithOperators() {
    doTest();
  }
  public void testAmpersandInDeclaration() {
    doTest();
  }
  public void testAmpersandInPropertyValue() {
    doTest();
  }
  public void testAmpersandBetweenTwoHashes () {
    doTest();
  }
  public void testDeclarationBlockInMedia() {
    doTest();
  }
  public void testDeclarationBlockInDocument() {
    doTest();
  }
  public void testDeclarationBlockInSupports() {
    doTest();
  }
  public void testDeclarationBlockInRegion() {
    doTest();
  }
  public void testDeclarationBlockInScope() {
    doTest();
  }
  public void testDeclarationBlockInBadAtRule() {
    doTest();
  }
}