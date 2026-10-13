package com.dmarcotte.handlebars.parsing;

import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.Test;

/**
 * Java representations of the validations in Handlebars spec/parser.js
 * (Precise revision: https://github.com/wycats/handlebars.js/blob/408192ba9f262bb82be88091ab3ec3c16dc02c6d/spec/parser.js)
 * <p/>
 * The tests here should map pretty clearly by name to the `it "does something"` validations in parser.js.
 * <p/>
 * See the docs on {@see HbParserTest} for info on how these tests work
 */
@TestApplication
public class HbParserSpecTest extends HbParserTest {

  @Test
  public void testSimpleMustaches() {
    doTest(true);
  }

  @Test
  public void testSimpleMustachesWithData() {
    doTest(true);
  }

  @Test
  public void testSimpleMustachesWithDataPaths() {
    doTest(true);
  }

  @Test
  public void testMustachesWithPaths() {
    doTest(true);
  }

  @Test
  public void testMustachesWithThisFoo() {
    doTest(true);
  }

  @Test
  public void testMustachesWithDashInPath() {
    doTest(true);
  }

  @Test
  public void testMustachesWithParameters() {
    doTest(true);
  }

  @Test
  public void testMustachesWithStringParameters() {
    doTest(true);
  }

  @Test
  public void testMustachesWithNumberParameters() {
    doTest(true);
  }

  @Test
  public void testMustachesWithKeywords() {
    doTest(true);
  }

  @Test
  public void testMustachesWithDataParameters() {
    doTest(true);
  }

  @Test
  public void testMustachesWithHashArguments() {
    doTest(true);
  }

  @Test
  public void testContentsFollowedByMustache() {
    doTest(true);
  }

  @Test
  public void testPartial() {
    doTest(true);
  }

  @Test
  public void testPartialWithContext() {
    doTest(true);
  }
  @Test
  public void testPartialWithParenthesis() { doTest(true);  }

  @Test
  public void testParsesPartialWithHash() {
    doTest(true);
  }

  @Test
  public void testParsesPartialWithContextAndHash() {
    doTest(true);
  }

  @Test
  public void testPartialWithComplexName() {
    doTest(true);
  }

  @Test
  public void testPartialBlocks() {
    doTest(true);
  }

  @Test
  public void testPartialBlockMismatch() {
    doTest(true);
  }

  @Test
  public void testPartialBlocksWithArguments() {
    doTest(true);
  }

  @Test
  public void testComment() {
    doTest(true);
  }

  @Test
  public void testMultiLineComment() {
    doTest(true);
  }

  @Test
  public void testInverseSection() {
    doTest(true);
  }

  @Test
  public void testInverseElseStyleSection() {
    doTest(true);
  }

  @Test
  public void testMultipleInverseSections() {
    doTest(true);
  }

  @Test
  public void testEmptyBlocks() {
    doTest(true);
  }

  @Test
  public void testEmptyBlocksWithEmptyInverseSection() {
    doTest(true);
  }

  @Test
  public void testEmptyBlocksWithEmptyInverseElseStyleSection() {
    doTest(true);
  }

  @Test
  public void testNonEmptyBlocksWithEmptyInverseSection() {
    doTest(true);
  }

  @Test
  public void testNonEmptyBlocksWithEmptyInverseElseStyleSection() {
    doTest(true);
  }

  @Test
  public void testEmptyBlocksWithNonEmptyInverseSection() {
    doTest(true);
  }

  @Test
  public void testEmptyBlocksWithNonEmptyInverseElseStyleSection() {
    doTest(true);
  }

  @Test
  public void testStandaloneInverseSection() {
    doTest(true);
  }

  @Test
  public void testOldStandaloneInverseSection() {
    doTest(true);
  }

  @Test
  public void testBlockWithBlockParams() {
    doTest(true);
  }

  @Test
  public void testInverseBlockWithBlockParams() {
    doTest(true);
  }

  @Test
  public void testChainedInverseBlockWithBlockParams() { doTest(true); }

  @Test
  public void testParseBlockDirectives() {
    doTest(true);
  }

  @Test
  public void testParseDirectives() {
    doTest(true);
  }

  @Test
  public void testFailIfDirectivesHaveInverse() {
    doTest(true);
  }

  /*
   * Note on the spec/parser.js porting: some tests at the end are omitted
   * because they make no sense in the context of the plugin
   */
}
