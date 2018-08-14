package org.angularjs.html;

import com.intellij.javascript.HtmlInlineJSScriptTokenTypesProvider;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.LanguageHtmlInlineScriptTokenTypesProvider;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.lang.javascript.JavascriptASTFactory;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XmlASTFactory;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.openapi.application.PathManager;
import com.intellij.psi.css.CssEmbeddedTokenTypesProvider;
import com.intellij.psi.css.CssRulesetBlockEmbeddedTokenTypesProvider;
import com.intellij.psi.css.impl.CssTreeElementFactory;
import com.intellij.psi.xml.StartTagEndTokenProvider;
import com.intellij.testFramework.ParsingTestCase;
import org.angular2.lang.html.parser.Angular2HtmlParserDefinition;

/**
 * @author Dennis.Ushakov
 */
public class Angular2HTMLParserTest extends ParsingTestCase {
  public Angular2HTMLParserTest() {
    super("", "html", true,
          new Angular2HtmlParserDefinition(), new JavascriptParserDefinition(), new CSSParserDefinition());
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
    addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, new CssTreeElementFactory());
    addExplicitExtension(LanguageHtmlInlineScriptTokenTypesProvider.INSTANCE, JavascriptLanguage.INSTANCE,
                         new HtmlInlineJSScriptTokenTypesProvider());
    registerExtensionPoint(StartTagEndTokenProvider.EP_NAME, StartTagEndTokenProvider.class);
    registerExtensionPoint(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider.class);
    registerExtension(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, new CssEmbeddedTokenTypesProvider());
    registerExtension(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, new CssRulesetBlockEmbeddedTokenTypesProvider());
  }

  public void testBinding() {
    doTest(true);
  }

  public void testEvent() {
    doTest(true);
  }

  public void testXmlText() {
    doTest(true);
  }

  public void testXmlAttribute() {
    doTest(true);
  }

  public void testBindingAttribute() {
    doTest(true);
  }

  public void testElvis() {
    doTest(true);
  }

  public void testCss() {
    doTest(true);
  }

  public void testWeb20713() {
    doTest(true);
  }

  public void testWeb24804() {
    doTest(true);
  }

  public void testEntity() {
    doTest(true);
  }
}