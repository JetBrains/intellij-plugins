package com.intellij.flex.parser;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSParsingTestBase;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.javascript.dialects.ECMAL4ParserDefinition;
import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.Test;

@TestApplication
public class ActionScriptParsingTest extends JSParsingTestBase {
  public ActionScriptParsingTest() {
    super(FlexTestUtils.getTestDataPath("parsing"), "js2", new ECMAL4ParserDefinition(), new JavascriptParserDefinition());
  }

  @Test
  public void testBasic() {
    doTest(true);
  }

  @Test
  public void testClassesPackagesNamespaces() {
    doTest(true);
  }

  @Test
  public void testQualifiedNsReference() {
    doTest(true);
  }

  @Test
  public void testAttributesInInclude() {
    doTest(true);
  }

  @Test
  public void testExpressions() {
    doTest(true);
  }

  @Test
  public void testE4X() {
    doTest(true);
  }

  @Test
  public void testE4X2() {
    doTest(true);
  }

  @Test
  public void testForEachIn() {
    doTest(true);
  }

  @Test
  public void testXmlDef() {
    doTest(true);
  }

  @Test
  public void testXmlDef2() {
    doTest(true);
  }

  @Test
  public void testObjectLiteral() {
    doTest(true);
  }

  @Test
  public void testAttributesListErrors() {
    doTest(true);
  }

  @Test
  public void testSeveralCatchesInTry() {
    doTest(true);
  }

  @Test
  public void testIncompleteCode() {
    doTest(true);
  }

  @Test
  public void testIncompleteCode2() {
    doTest(true);
  }

  @Test
  public void testIncompleteNewJS2() {
    doTest(true);
  }

  @Test
  public void testOldAs2Code() {
    doTest(true);
  }

  @Test
  public void testXmlAttributeSelectionInWithStatement() {
    doTest(true);
  }

  @Test
  public void testKeywordsAsClassAndFunctionName() {
    doTest(true);
  }

  @Test
  public void testArrayLiterals() {
    doTest(true);
  }

  @Test
  public void testKeywordNsReference() {
    doTest(true);
  }

  @Test
  public void testKeywordsInE4X() {
    doTest(true);
  }

  @Test
  public void testConditionalBlocks() {
    doTest(true);
  }

  @Test
  public void testActionScriptSpecific_() {
    doTest(true);
  }

  @Test
  public void testVectorInitializers() {
    doTest(true);
  }

  @Test
  public void testGenerics() {
    doTest(true);
  }

  @Test
  public void testGenerics2() {
    doTest(true);
  }

  @Test
  public void testIncorrectGenerics() {
    doTest(true);
  }

  @Test
  public void testComplex() {
    doTest(true);
  }

  @Test
  public void testE4X3() {
    doTest(true);
  }

  @Test
  public void testMiscGotchas() {
    doTest(true);
  }

  @Test
  public void testNoTypeRefInExtends() {
    doTest(true);
  }

  @Test
  public void testStaticBlock() {
    doTest(true);
  }

  @Test
  public void testNoReferenceBeforeQuotedAttributeValue() {
    doTest(true);
  }

  @Test
  public void testConstInFor() {
    doTest(true);
  }

  @Test
  public void testASDoc() {
    doTest(true);
  }

  @Test
  public void testUseNsInAttrList() {
    doTest(true);
  }

  @Test
  public void testLiteralWithExpressionProperties() {
    doTest(true);
  }

  @Test
  public void testGoto() {
    doTest(true);
  }

  @Test
  public void testBroken() {
    doTest(true);
  }

}