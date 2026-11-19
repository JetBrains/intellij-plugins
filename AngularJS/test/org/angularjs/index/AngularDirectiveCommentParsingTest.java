package org.angularjs.index;

import org.junit.jupiter.api.Test;

import static org.angularjs.index.AngularJSIndexingHandler.getParamValue;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class AngularDirectiveCommentParsingTest {

  @Test
  public void testDirectiveDocParsing() {
    assertEquals("E", getParamValue("", "E"));
    assertEquals("", getParamValue("", ""));
    assertEquals("", getParamValue("", "//"));
    assertEquals("ANY", getParamValue("", "ANY //tag"));
  }
}
