package org.angularjs.html;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.openapi.application.PathManager;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.testFramework.LexerTestCase;
import org.angularjs.AngularTestUtil;
import org.angularjs.lang.parser.AngularJSElementTypes;

/**
 * @author Dennis.Ushakov
 */
public class Angular2InterpolationsLexerTest extends LexerTestCase {
  public void testInterpolation() {
    doTest("{{interpolated}}", "XML_DATA_CHARACTERS ('{{')\n" +
                               "ANG_EMBEDDED_CONTENT ('interpolated')\n" +
                               "XML_DATA_CHARACTERS ('}}')");

    doTest("{{interpolated}}{{again}}", "XML_DATA_CHARACTERS ('{{')\n" +
                                        "ANG_EMBEDDED_CONTENT ('interpolated')\n" +
                                        "XML_DATA_CHARACTERS ('}}{{')\n" +
                                        "ANG_EMBEDDED_CONTENT ('again')\n" +
                                        "XML_DATA_CHARACTERS ('}}')");

    doTest("{{interpolated}}with{{text}}", "XML_DATA_CHARACTERS ('{{')\n" +
                                           "ANG_EMBEDDED_CONTENT ('interpolated')\n" +
                                           "XML_DATA_CHARACTERS ('}}with{{')\n" +
                                           "ANG_EMBEDDED_CONTENT ('text')\n" +
                                           "XML_DATA_CHARACTERS ('}}')");

    doTest("more{{interpolated}}with{{text}}again", "XML_DATA_CHARACTERS ('more{{')\n" +
                                                    "ANG_EMBEDDED_CONTENT ('interpolated')\n" +
                                                    "XML_DATA_CHARACTERS ('}}with{{')\n" +
                                                    "ANG_EMBEDDED_CONTENT ('text')\n" +
                                                    "XML_DATA_CHARACTERS ('}}again')");
  }

  public void testWithLineBreaks() {
    doTest("{{#todo of todoService.todos\n" +
           "            | started : status\n" +
           "            | search : term\n" +
           "            }}", "XML_DATA_CHARACTERS ('{{')\n" +
                             "ANG_EMBEDDED_CONTENT ('#todo of todoService.todos\\n            | started : status\\n            | search : term\\n            ')\n" +
                             "XML_DATA_CHARACTERS ('}}')");
  }

  @Override
  protected Lexer createLexer() {
    final _AngularJSInterpolationsLexer lexer = new _AngularJSInterpolationsLexer(null);
    lexer.setType(XmlTokenType.XML_DATA_CHARACTERS);
    return new MergingLexerAdapter(new FlexAdapter(lexer), TokenSet.create(AngularJSElementTypes.EMBEDDED_CONTENT, XmlTokenType.XML_DATA_CHARACTERS));
  }

  @Override
  protected String getDirPath() {
    return AngularTestUtil.getBaseTestDataPath(Angular2InterpolationsLexerTest.class).substring(PathManager.getHomePath().length());
  }
}
