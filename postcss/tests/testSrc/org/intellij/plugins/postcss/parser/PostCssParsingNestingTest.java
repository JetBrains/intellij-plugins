package org.intellij.plugins.postcss.parser;

import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.Test;

@TestApplication
public class PostCssParsingNestingTest extends PostCssParsingTest {
  public PostCssParsingNestingTest() {
    super("nesting");
  }

  @Test
  public void testNestingAmpersand() {
    doTest();
  }
  @Test
  public void testNestingNestRule() {
    doTest();
  }
  @Test
  public void testSemicolonsCorrect() {
    doTest();
  }
  @Test
  public void testSemicolonsIncorrect() {
    doTest();
  }
  @Test
  public void testDirectNestingIncorrect() {
    doTest();
  }
  @Test
  public void testTopLevelRulesetNesting() {
    doTest();
  }
  @Test
  public void testNestRulesetInsideAtRule() {
    doTest();
  }
  @Test
  public void testPartOfNestKeywordTopLevel() {
    doTest();
  }
  @Test
  public void testPartOfNestKeywordInsideRuleset() {
    doTest();
  }
  @Test
  public void testPartOfNestKeywordInsideAtRule() {
    doTest();
  }
  @Test
  public void testPartOfNestKeywordInsidePageAtRule() {
    doTest();
  }
  @Test
  public void testPartOfNestInsideApplyFunction() {
    doTest();
  }
  @Test
  public void testNestAtRuleIncorrectSelectorList() {
    doTest();
  }
  @Test
  public void testAmpersandInSimpleSelector() {
    doTest();
  }
  @Test
  public void testAmpersandInClass() {
    doTest();
  }
  @Test
  public void testAmpersandIdSelector() {
    doTest();
  }
  @Test
  public void testAmpersandInPseudoClasses() {
    doTest();
  }
  @Test
  public void testAmpersandInPseudoFunction() {
    doTest();
  }
  @Test
  public void testAmpersandInAttributes() {
    doTest();
  }
  @Test
  public void testAmpersandWithOperators() {
    doTest();
  }
  @Test
  public void testAmpersandInDeclaration() {
    doTest();
  }
  @Test
  public void testAmpersandInPropertyValue() {
    doTest();
  }
  @Test
  public void testAmpersandBetweenTwoHashes () {
    doTest();
  }
  @Test
  public void testDeclarationBlockInMedia() {
    doTest();
  }
  @Test
  public void testDeclarationBlockInDocument() {
    doTest();
  }
  @Test
  public void testDeclarationBlockInSupports() {
    doTest();
  }
  @Test
  public void testDeclarationBlockInRegion() {
    doTest();
  }
  @Test
  public void testDeclarationBlockInScope() {
    doTest();
  }
  @Test
  public void testDeclarationBlockInBadAtRule() {
    doTest();
  }
}