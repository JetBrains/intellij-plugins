package org.angularjs.html;

import com.intellij.javascript.HtmlInlineJSScriptTokenTypesProvider;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.LanguageHtmlInlineScriptTokenTypesProvider;
import com.intellij.lang.javascript.JavascriptASTFactory;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XmlASTFactory;
import com.intellij.lexer.HtmlEmbeddedTokenTypesProvider;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.xml.StartTagEndTokenProvider;
import com.intellij.testFramework.ParsingTestCase;

/**
 * @author Dennis.Ushakov
 */
public class Angular2HTMLParserTest extends ParsingTestCase {
  public Angular2HTMLParserTest() {
    super("", "html", true,
          new Angular2HTMLParserDefinition(), new JavascriptParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + "/contrib/AngularJS/test/org/angularjs/html/data";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    addExplicitExtension(LanguageASTFactory.INSTANCE, JavascriptLanguage.INSTANCE, new JavascriptASTFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, new XmlASTFactory());
    registerExtensionPoint(HtmlEmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, HtmlEmbeddedTokenTypesProvider.class);
    addExplicitExtension(LanguageHtmlInlineScriptTokenTypesProvider.INSTANCE, JavascriptLanguage.INSTANCE,
                         new HtmlInlineJSScriptTokenTypesProvider());
    registerExtensionPoint(new ExtensionPointName<StartTagEndTokenProvider>("com.intellij.xml.startTagEndToken"),
                           StartTagEndTokenProvider.class);

  }

  public void testBinding() throws Exception {
    doTest(true);
  }

  public void testEvent() throws Exception {
    doTest(true);
  }
}