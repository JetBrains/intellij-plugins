package com.dmarcotte.handlebars.parsing;

import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.Test;

/**
 * Free form parser tests, mostly to pin down regression and corner cases
 * <p/>
 * See the docs on {@see HbParserTest} for info on how these tests work
 */
@TestApplication
public class HbParserFreeFormTest extends HbParserTest {
  @Test
  public void testSampleFullFile1() {
    doTest(true);
  }

  @Test
  public void testSampleFullFile2() {
    doTest(true);
  }

  @Test
  public void testManyIdsFollowedByParam() {
    doTest(true);
  }

  @Test
  public void testUnclosedMustacheFollowedByMustache() {
    doTest(true);
  }

  @Test
  public void testOpenInverseVsSimpleInverse() {
    doTest(true);
  }

  @Test
  public void testParamWithNoId() {
    doTest(true);
  }

  @Test
  public void testNestedMustaches() {
    doTest(true);
  }

  @Test
  public void testPoorlyNestedMustaches() {
    doTest(true);
  }

  @Test
  public void testOpenInverse() {
    doTest(true);
  }

  @Test
  public void testPathsEndingInDot() {
    doTest(true);
  }

  @Test
  public void testNoOpenQuote() {
    doTest(true);
  }

  @Test
  public void testInvalidCharacters() {
    doTest(true);
  }

  @Test
  public void testCloseNotFollowingOpen() {
    doTest(true);
  }

  /**
   * Empty blocks should not be errors.  (For a while, the parser was flagging {{#foo}}{{/foo}}-type blocks)
   *
   * Some of these are now also tested in ParserSpec.  This is the original regression test
   * from before those were added to handlebars.js
   */
  @Test
  public void testEmptyBlockRegression() {
    doTest(true);
  }

  @Test
  public void testUnclosedSimpleComment() {
    doTest(true);
  }

  @Test
  public void testUnclosedBlockComment() {
    doTest(true);
  }

  @Test
  public void testParamWithDecimal() {
    doTest(true);
  }

  @Test
  public void testSubexpressions() {
    doTest(true);
  }

  @Test
  public void testRawBlock() {
    doTest(true);
  }

  @Test
  public void testRawBlockParameters() {
    doTest(true);
  }

  @Test
  public void testDataFunctionsWithParams() {
    doTest(true);
  }

  @Test
  public void testLiteralHelperName() {
    doTest(true);
  }

  @Test
  public void testInheritanceBlock() {
    doTest(true);
  }
}
