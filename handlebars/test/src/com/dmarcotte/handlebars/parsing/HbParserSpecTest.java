package com.dmarcotte.handlebars.parsing;

/**
 * Java representations of the validations in Handlebars parser_spec.rb
 * (Precise revision: https://github.com/wycats/handlebars.js/blob/932e2970ad29b16d6d6874ad0bfb44b07b4cd765/spec/parser_spec.rb)
 * <p/>
 * The tests here should map pretty clearly by name to the `it "does something"` validations in parser_spec.rb.
 * <p/>
 * See the docs on {@see HbParserTest} for info on how these tests work
 */
public class HbParserSpecTest extends HbParserTest {

  public void testSimpleMustaches() {
    doTest(true);
  }

  public void testMustachesWithPaths() {
    doTest(true);
  }

  // TODO testMustachesWithThisFoo is actually a bit odd... parser_spec.rb expects just an id of foo, but our parser gives id,sep,id for this,/,foo
  public void testMustachesWithThisFoo() {
    doTest(true);
  }

  public void testMustachesWithDashInPath() {
    doTest(true);
  }

  public void testMustachesWithParameters() {
    doTest(true);
  }

  public void testMustachesWithHashArguments() {
    doTest(true);
  }

  public void testMustachesWithStringParameters() {
    doTest(true);
  }

  public void testMustachesWithIntegerParameters() {
    doTest(true);
  }

  public void testMustachesWithBooleanParameters() {
    doTest(true);
  }

  public void testSimpleMustachesWithData() {
    doTest(true);
  }

  public void testMustachesWithDataParameters() {
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

  public void testStandaloneInverseSection() {
    doTest(true);
  }
}
