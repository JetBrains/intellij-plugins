package com.jetbrains.typoscript;

import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.ParsingTestCase;
import com.jetbrains.typoscript.lang.TypoScriptParserDefinition;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;

/**
 * @author lene
 *         Date: 05.03.13
 */
public class TypoScriptParserTest extends ParsingTestCase {

  public TypoScriptParserTest() {
    super("parser", "ts", new TypoScriptParserDefinition());
  }

  public void testAssignments() throws IOException {
    doTest(true);
  }

  public void testCodeBlock() throws IOException {
    doTest(true);
  }

  public void testComments() throws IOException {
    doTest(true);
  }

  public void testCondition() throws IOException {
    doTest(true);
  }

  public void testMultilineValue() throws IOException {
    doTest(true);
  }

  public void testExample() throws IOException {
    doTest(true);
  }

  public void testExample2() throws IOException {
    doTest(true);
  }

  public void testExample3() throws IOException {
    doTest(true);
  }

  public void testExample4() throws IOException {
    doTest(true);
  }


  @Override
  @NonNls
  protected String getTestDataPath() {
    return PathManager.getHomePath() + TypoScriptTestUtil.getDataSubPath("");
  }
}
