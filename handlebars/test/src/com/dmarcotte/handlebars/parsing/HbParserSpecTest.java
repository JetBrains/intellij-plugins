package com.dmarcotte.handlebars.parsing;

/**
 * Java representations of the validations in Handlebars spec/parser.js
 * (Precise revision: https://github.com/wycats/handlebars.js/blob/cb22ee5681b1eb1f89ee675651c018b77dd1524d/spec/parser.js)
 * <p/>
 * The tests here should map pretty clearly by name to the `it "does something"` validations in parser.js.
 * <p/>
 * See the docs on {@see HbParserTest} for info on how these tests work
 */
public class HbParserSpecTest extends HbParserTest {

  public void testSimpleMustaches() {
    doTest(true);
  }

  public void testSimpleMustachesWithData() {
    doTest(true);
  }

  public void testSimpleMustachesWithDataPaths() {
    doTest(true);
  }

  public void testMustachesWithPaths() {
    doTest(true);
  }

  public void testMustachesWithThisFoo() {
    doTest(true);
  }

  public void testMustachesWithDashInPath() {
    doTest(true);
  }

  public void testMustachesWithParameters() {
    doTest(true);
  }

  public void testMustachesWithStringParameters() {
    doTest(true);
  }

  public void testMustachesWithNumberParameters() {
    doTest(true);
  }

  public void testMustachesWithBooleanParameters() {
    doTest(true);
  }

  public void testMustachesWithDataParameters() {
    doTest(true);
  }

  public void testMustachesWithHashArguments() {
    doTest(true);
  }

  public void testContentsFollowedByMustache() {
    doTest(true);
  }

  public void testPartial() {
    doTest(true);
  }

  public void testPartialWithContext() {
    doTest(true);
  }

  public void testParsesPartialWithHash() {
    doTest(true);
  }

  public void testParsesPartialWithContextAndHash() {
    doTest(true);
  }

  public void testPartialWithComplexName() {
    doTest(true);
  }

  public void testComment() {
    doTest(true);
  }

  public void testMultiLineComment() {
    doTest(true);
  }

  public void testInverseSection() {
    doTest(true);
  }

  public void testInverseElseStyleSection() {
    doTest(true);
  }

  public void testEmptyBlocks() {
    doTest(true);
  }

  public void testEmptyBlocksWithEmptyInverseSection() {
    doTest(true);
  }

  public void testEmptyBlocksWithEmptyInverseElseStyleSection() {
    doTest(true);
  }

  public void testNonEmptyBlocksWithEmptyInverseSection() {
    doTest(true);
  }

  public void testNonEmptyBlocksWithEmptyInverseElseStyleSection() {
    doTest(true);
  }

  public void testEmptyBlocksWithNonEmptyInverseSection() {
    doTest(true);
  }

  public void testEmptyBlocksWithNonEmptyInverseElseStyleSection() {
    doTest(true);
  }

  public void testStandaloneInverseSection() {
    doTest(true);
  }

  /**
   * Note on the spec/parser.js porting: some tests at the end are omitted
   * because they make no sense in the context of the plugin
   */
}
