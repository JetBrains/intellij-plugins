package com.dmarcotte.handlebars.parsing;

/**
 * Free form parser tests, mostly to pin down regression and corner cases
 * <p/>
 * See the docs on {@see HbParserTest} for info on how these tests work
 */
public class HbParserFreeFormTest extends HbParserTest {
  public void testSampleFullFile1() {
    doTest(true);
  }

  public void testSampleFullFile2() {
    doTest(true);
  }

  public void testManyIdsFollowedByParam() {
    doTest(true);
  }

  public void testUnclosedMustacheFollowedByMustache() {
    doTest(true);
  }

  public void testOpenInverseVsSimpleInverse() {
    doTest(true);
  }

  public void testParamWithNoId() {
    doTest(true);
  }

  public void testNestedMustaches() {
    doTest(true);
  }

  public void testPoorlyNestedMustaches() {
    doTest(true);
  }

  public void testOpenInverse() {
    doTest(true);
  }

  public void testPathsEndingInDot() {
    doTest(true);
  }

  public void testNoOpenQuote() {
    doTest(true);
  }

  public void testInvalidCharacters() {
    doTest(true);
  }

  public void testCloseNotFollowingOpen() {
    doTest(true);
  }

  /**
   * Empty blocks should not be errors.  (For a while, the parser was flagging {{#foo}}{{/foo}}-type blocks)
   */
  public void testEmptyBlocks() {
    doTest(true);
  }

  public void testUnclosedSimpleComment() {
    doTest(true);
  }

  public void testUnclosedBlockComment() {
    doTest(true);
  }
}
