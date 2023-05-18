package com.intellij.flex.parser;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.javascript.JSParsingTestBase;
import com.intellij.lang.javascript.JavascriptASTFactory;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.javascript.dialects.ECMAL4ParserDefinition;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XmlASTFactory;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.xml.StartTagEndTokenProvider;

public class ActionScriptParsingTest extends JSParsingTestBase {
  public ActionScriptParsingTest() {
    super("", "js2", new ECMAL4ParserDefinition(), new JavascriptParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    addExplicitExtension(LanguageASTFactory.INSTANCE, JavascriptLanguage.INSTANCE, new JavascriptASTFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, new XmlASTFactory());
    registerExtensionPoint(new ExtensionPointName<>("com.intellij.xml.startTagEndToken"),
                           StartTagEndTokenProvider.class);
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("parsing");
  }

  public void testBasic() {
    doTest(true);
  }

  public void testClassesPackagesNamespaces() {
    doTest(true);
  }

  public void testQualifiedNsReference() {
    doTest(true);
  }

  public void testAttributesInInclude() {
    doTest(true);
  }

  public void testExpressions() {
    doTest(true);
  }

  public void testE4X() {
    doTest(true);
  }

  public void testE4X2() {
    doTest(true);
  }

  public void testForEachIn() {
    doTest(true);
  }

  public void testXmlDef() {
    doTest(true);
  }

  public void testXmlDef2() {
    doTest(true);
  }

  public void testObjectLiteral() {
    doTest(true);
  }

  public void testAttributesListErrors() {
    doTest(true);
  }

  public void testSeveralCatchesInTry() {
    doTest(true);
  }

  public void testIncompleteCode() {
    doTest(true);
  }

  public void testIncompleteCode2() {
    doTest(true);
  }

  public void testIncompleteNewJS2() {
    doTest(true);
  }

  public void testOldAs2Code() {
    doTest(true);
  }

  public void testXmlAttributeSelectionInWithStatement() {
    doTest(true);
  }

  public void testKeywordsAsClassAndFunctionName() {
    doTest(true);
  }

  public void testArrayLiterals() {
    doTest(true);
  }

  public void testKeywordNsReference() {
    doTest(true);
  }

  public void testKeywordsInE4X() {
    doTest(true);
  }

  public void testConditionalBlocks() {
    doTest(true);
  }

  public void testActionScriptSpecific_() {
    doTest(true);
  }

  public void testVectorInitializers() {
    doTest(true);
  }

  public void testGenerics() {
    doTest(true);
  }

  public void testGenerics2() {
    doTest(true);
  }

  public void testIncorrectGenerics() {
    doTest(true);
  }

  public void testComplex() {
    doTest(true);
  }

  public void testE4X3() {
    doTest(true);
  }

  public void testMiscGotchas() {
    doTest(true);
  }

  public void testNoTypeRefInExtends() {
    doTest(true);
  }

  public void testStaticBlock() {
    doTest(true);
  }

  public void testNoReferenceBeforeQuotedAttributeValue() {
    doTest(true);
  }

  public void testConstInFor() {
    doTest(true);
  }

  public void testASDoc() {
    doTest(true);
  }

  public void testUseNsInAttrList() {
    doTest(true);
  }

  public void testLiteralWithExpressionProperties() {
    doTest(true);
  }

  public void testGoto() {
    doTest(true);
  }

  public void testBroken() {
    doTest(true);
  }

}