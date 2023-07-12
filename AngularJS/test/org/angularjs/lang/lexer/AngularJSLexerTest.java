package org.angularjs.lang.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.LexerTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSLexerTest extends LexerTestCase {
  public void testIdent() {
    doFileTest("js");
  }

  public void testKey_value() {
    doFileTest("js");
  }

  public void testExpr() {
    doFileTest("js");
  }

  public void testKeyword() {
    doFileTest("js");
  }

  public void testNumber() {
    doFileTest("js");
  }

  public void testString() {
    doFileTest("js");
  }

  @Override
  protected @NotNull Lexer createLexer() {
    return new AngularJSLexer();
  }

  @Override
  protected @NotNull String getDirPath() {
    return AngularTestUtil.getBaseTestDataPath(AngularJSLexerTest.class).substring(PathManager.getHomePath().length());
  }
}