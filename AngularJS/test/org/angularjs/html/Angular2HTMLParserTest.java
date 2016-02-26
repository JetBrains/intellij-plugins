package org.angularjs.html;

import com.intellij.javascript.HtmlInlineJSScriptTokenTypesProvider;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.LanguageHtmlInlineScriptTokenTypesProvider;
import com.intellij.lang.javascript.JavascriptASTFactory;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XmlASTFactory;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.openapi.application.PathManager;
import com.intellij.psi.xml.StartTagEndTokenProvider;
import com.intellij.testFramework.ParsingTestCase;
import org.angularjs.lang.parser.AngularJSParserDefinition;

/**
 * @author Dennis.Ushakov
 */
public class Angular2HTMLParserTest extends ParsingTestCase {
  public Angular2HTMLParserTest() {
    super("", "html", true,
          new Angular2HTMLParserDefinition(), new JavascriptParserDefinition(), new AngularJSParserDefinition());
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
    addExplicitExtension(LanguageHtmlInlineScriptTokenTypesProvider.INSTANCE, JavascriptLanguage.INSTANCE,
                         new HtmlInlineJSScriptTokenTypesProvider());
    registerExtensionPoint(StartTagEndTokenProvider.EP_NAME, StartTagEndTokenProvider.class);
    registerExtensionPoint(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider.class);
  }

  public void testBinding() throws Exception {
    doTest(true);
  }

  public void testEvent() throws Exception {
    doTest(true);
  }

  public void testXmlText() throws Exception {
    doTest(true);
  }

  public void testXmlAttribute() throws Exception {
    doTest(true);
  }

  public void testBindingAttribute() throws Exception {
    doTest(true);
  }

  public void testElvis() throws Exception {
    doTest(true);
  }
}