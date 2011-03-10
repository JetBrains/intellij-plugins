package com.intellij.flex.uiDesigner;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class AbcFilterTest extends TestCase {
  public void testReplaceMainClass() throws IOException {
    AbcFilter filter = new AbcFilter();
    filter.replaceMainClass = true;
    File out = File.createTempFile("abc_", ".swf");
//    File out = new File("/Users/develar/o.swf");
    Collection<CharSequence> unneededClasses = new ArrayList<CharSequence>(1);
    unneededClasses.add("_106eaa1eb9b638812e6dfdfc0cb9d2f6444a984f2e74be374021d84c9b5fab37_mx_core_FlexModuleFactory");
    filter.filter(new File(FlexUIDesignerBaseTestCase.getTestDataPathImpl() + "/libraryWithIncompatibleMxFlexModuleFactory.swf"), out, new AbcNameFilterByNameSet(unneededClasses));
    assertEquals(out.length(), 484567);
  }
}
