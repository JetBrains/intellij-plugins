package org.angularjs.index;

import junit.framework.TestCase;

import static org.angularjs.index.AngularJSIndexingHandler.getParamValue;


public class AngularDirectiveCommentParsingTest extends TestCase {

  public void testDirectiveDocParsing() {
    assertEquals("E", getParamValue("", "E"));
    assertEquals("", getParamValue("", ""));
    assertEquals("", getParamValue("", "//"));
    assertEquals("ANY", getParamValue("", "ANY //tag"));
  }
}
