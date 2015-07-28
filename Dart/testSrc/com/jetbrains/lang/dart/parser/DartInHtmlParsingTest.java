package com.jetbrains.lang.dart.parser;

import com.intellij.lang.HtmlInlineScriptTokenTypesProvider;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.LanguageHtmlScriptContentProvider;
import com.intellij.lang.dtd.DTDLanguage;
import com.intellij.lang.dtd.DTDParserDefinition;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.lang.xml.XmlASTFactory;
import com.intellij.lexer.HtmlEmbeddedTokenTypesProvider;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.xml.StartTagEndTokenProvider;
import com.intellij.testFramework.ParsingTestCase;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartScriptContentProvider;
import com.jetbrains.lang.dart.util.DartTestUtils;

/**
 * @author: Fedor.Korotkov
 */
public class DartInHtmlParsingTest extends ParsingTestCase {
  public DartInHtmlParsingTest() {
    super("parsing/html", "html");
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    configureFromParserDefinition(new XMLParserDefinition(), "xml");
    configureFromParserDefinition(new DTDParserDefinition(), "dtd");
    configureFromParserDefinition(new HTMLParserDefinition(), "html");
    addExplicitExtension(LanguageASTFactory.INSTANCE, HTMLLanguage.INSTANCE, new XmlASTFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, new XmlASTFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, DTDLanguage.INSTANCE, new XmlASTFactory());
    registerExtensionPoint(new ExtensionPointName<StartTagEndTokenProvider>("com.intellij.xml.startTagEndToken"),
                           StartTagEndTokenProvider.class);
    registerExtensionPoint(new ExtensionPointName<HtmlEmbeddedTokenTypesProvider>("com.intellij.html.embeddedTokenTypesProvider"),
                           HtmlEmbeddedTokenTypesProvider.class);
    registerExtensionPoint(new ExtensionPointName<HtmlInlineScriptTokenTypesProvider>("com.intellij.html.inlineScriptTokenTypesProvider"),
                           HtmlInlineScriptTokenTypesProvider.class);
    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, DartLanguage.INSTANCE, new DartScriptContentProvider());
  }

  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  public void testHtml1() throws Throwable {
    doTest(true);
  }
}
