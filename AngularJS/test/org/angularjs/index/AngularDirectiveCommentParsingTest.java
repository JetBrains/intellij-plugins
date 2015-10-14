package org.angularjs.index;

import com.intellij.testFramework.UsefulTestCase;

import static org.angularjs.index.AngularJSIndexingHandler.RESTRICT;
import static org.angularjs.index.AngularJSIndexingHandler.getParamValue;


public class AngularDirectiveCommentParsingTest extends UsefulTestCase {

  public void testDirectiveDocParsing() {
    assertEquals("ANY", getParamValue("", " * @restrict ANY    ", RESTRICT).trim());
    assertEquals("ANY", getParamValue("", "@restrict ANY    ", RESTRICT).trim());
    assertEquals("E", getParamValue("", "* @restrict E", RESTRICT).trim());
    assertEquals("E", getParamValue("", "* @restrictE", RESTRICT).trim());
    assertEquals("", getParamValue("", "@restrict", RESTRICT).trim());
    assertEquals("", getParamValue("", "@restrict//", RESTRICT).trim());
    assertEquals("ANY", getParamValue("", " * @restrict ANY //tag", RESTRICT));
  }
}
