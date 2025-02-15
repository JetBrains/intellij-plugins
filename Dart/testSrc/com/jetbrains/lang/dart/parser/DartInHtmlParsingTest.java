package com.jetbrains.lang.dart.parser;

import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.LanguageHtmlScriptContentProvider;
import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XmlASTFactory;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.psi.xml.StartTagEndTokenProvider;
import com.intellij.testFramework.ParsingTestCase;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartParserDefinition;
import com.jetbrains.lang.dart.DartScriptContentProvider;
import com.jetbrains.lang.dart.util.DartTestUtils;

import static com.intellij.xml.XmlElementTypeServiceHelper.registerXmlElementTypeServices;

public class DartInHtmlParsingTest extends ParsingTestCase {
  public DartInHtmlParsingTest() {
    super("parsing/html", "html", new HTMLParserDefinition(), new DartParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    registerXmlElementTypeServices(getApplication(), getTestRootDisposable());
    addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, new XmlASTFactory());
    registerExtensionPoint(StartTagEndTokenProvider.EP_NAME, StartTagEndTokenProvider.class);
    registerExtensionPoint(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider.class);
    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, DartLanguage.INSTANCE, new DartScriptContentProvider());
  }

  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  public void testHtml1() {
    doTest(true);
  }
}
