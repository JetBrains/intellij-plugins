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

  public void testBasic() throws Exception {
    doTest(true);
  }

  public void testClassesPackagesNamespaces() throws Exception {
    doTest(true);
  }

  public void testQualifiedNsReference() throws Exception {
    doTest(true);
  }

  public void testAttributesInInclude() throws Exception {
    doTest(true);
  }

  public void testExpressions() throws Exception {
    doTest(true);
  }

  public void testE4X() throws Exception {
    doTest(true);
  }

  public void testE4X2() throws Exception {
    doTest(true);
  }

  public void testForEachIn() throws Exception {
    doTest(true);
  }

  public void testXmlDef() throws Exception {
    doTest(true);
  }

  public void testXmlDef2() throws Exception {
    doTest(true);
  }

  public void testObjectLiteral() throws Exception {
    doTest(true);
  }

  public void testAttributesListErrors() throws Exception {
    doTest(true);
  }

  public void testSeveralCatchesInTry() throws Exception {
    doTest(true);
  }

  public void testIncompleteCode() throws Exception {
    doTest(true);
  }

  public void testIncompleteCode2() throws Exception {
    doTest(true);
  }

  public void testIncompleteNewJS2() throws Exception {
    doTest(true);
  }

  public void testOldAs2Code() throws Exception {
    doTest(true);
  }

  public void testXmlAttributeSelectionInWithStatement() throws Exception {
    doTest(true);
  }

  public void testKeywordsAsClassAndFunctionName() throws Exception {
    doTest(true);
  }

  public void testArrayLiterals() throws Exception {
    doTest(true);
  }

  public void testKeywordNsReference() throws Exception {
    doTest(true);
  }

  public void testKeywordsInE4X() throws Exception {
    doTest(true);
  }

  public void testConditionalBlocks() throws Exception {
    doTest(true);
  }

  public void testActionScriptSpecific_() throws Exception {
    doTest(true);
  }

  public void testVectorInitializers() throws Exception {
    doTest(true);
  }

  public void testGenerics() throws Exception {
    doTest(true);
  }

  public void testGenerics2() throws Exception {
    doTest(true);
  }

  public void testIncorrectGenerics() throws Exception {
    doTest(true);
  }

  public void testComplex() throws Exception {
    doTest(true);
  }

  public void testE4X3() throws Exception {
    doTest(true);
  }

  public void testMiscGotchas() throws Exception {
    doTest(true);
  }

  public void testNoTypeRefInExtends() throws Exception {
    doTest(true);
  }

  public void testStaticBlock() throws Exception {
    doTest(true);
  }

  public void testNoReferenceBeforeQuotedAttributeValue() throws Exception {
    doTest(true);
  }

  public void testConstInFor() throws Exception {
    doTest(true);
  }

  public void testASDoc() throws Exception {
    doTest(true);
  }

  public void testUseNsInAttrList() throws Exception {
    doTest(true);
  }

  public void testLiteralWithExpressionProperties() throws Exception {
    doTest(true);
  }

  public void testGoto() throws Exception {
    doTest(true);
  }
}