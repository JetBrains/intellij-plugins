package org.angularjs.lang.lexer;

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixtures;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixtureKt.lexerFixture;

/**
 * @author Dennis.Ushakov
 */
@TestFixtures
public class AngularJSLexerTest {
  private final TestFixture<LexerTestFixture> lexer =
    lexerFixture(AngularTestUtil.getBaseTestDataPath(AngularJSLexerTest.class).replaceAll("/$", ""), () -> new AngularJSLexer());

  @Test
  public void testIdent() {
    lexer.get().doFileTest("js");
  }

  @Test
  public void testKey_value() {
    lexer.get().doFileTest("js");
  }

  @Test
  public void testExpr() {
    lexer.get().doFileTest("js");
  }

  @Test
  public void testKeyword() {
    lexer.get().doFileTest("js");
  }

  @Test
  public void testNumber() {
    lexer.get().doFileTest("js");
  }

  @Test
  public void testString() {
    lexer.get().doFileTest("js");
  }
}
