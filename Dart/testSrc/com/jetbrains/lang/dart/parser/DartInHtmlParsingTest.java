package com.jetbrains.lang.dart.parser;

import com.intellij.lang.HtmlInlineScriptTokenTypesProvider;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.LanguageHtmlScriptContentProvider;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.dtd.DTDLanguage;
import com.intellij.lang.dtd.DTDParserDefinition;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.lang.xml.XmlASTFactory;
import com.intellij.lexer.HtmlEmbeddedTokenTypesProvider;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.xml.XmlChildRole;
import com.intellij.testFramework.ParsingTestCase;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartScriptContentProvider;

/**
 * @author: Fedor.Korotkov
 */
public class DartInHtmlParsingTest extends ParsingTestCase {
  public DartInHtmlParsingTest() {
    super("parsing/html", "html");
  }

  @Override
  protected void setUp() throws Exception {
    myLanguage = HTMLLanguage.INSTANCE;
    super.setUp();
    addExplicitExtension(LanguageParserDefinitions.INSTANCE, XMLLanguage.INSTANCE, new XMLParserDefinition());
    addExplicitExtension(LanguageParserDefinitions.INSTANCE, DTDLanguage.INSTANCE, new DTDParserDefinition());
    addExplicitExtension(LanguageParserDefinitions.INSTANCE, HTMLLanguage.INSTANCE, new HTMLParserDefinition());
    addExplicitExtension(LanguageASTFactory.INSTANCE, HTMLLanguage.INSTANCE, new XmlASTFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, new XmlASTFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, DTDLanguage.INSTANCE, new XmlASTFactory());
    registerExtensionPoint(new ExtensionPointName<XmlChildRole.StartTagEndTokenProvider>("com.intellij.xml.startTagEndToken"),
                           XmlChildRole.StartTagEndTokenProvider.class);
    registerExtensionPoint(new ExtensionPointName<HtmlEmbeddedTokenTypesProvider>("com.intellij.html.embeddedTokenTypesProvider"),
                           HtmlEmbeddedTokenTypesProvider.class);
    registerExtensionPoint(new ExtensionPointName<HtmlInlineScriptTokenTypesProvider>("com.intellij.html.inlineScriptTokenTypesProvider"),
                           HtmlInlineScriptTokenTypesProvider.class);
    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, DartLanguage.INSTANCE, new DartScriptContentProvider());
  }

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/");
  }

  public void testHtml1() throws Throwable {
    doTest(true);
  }
}
