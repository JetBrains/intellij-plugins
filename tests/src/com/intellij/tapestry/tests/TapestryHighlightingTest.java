package com.intellij.tapestry.tests;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.tapestry.intellij.inspections.TelReferencesInspection;
import com.intellij.codeInspection.xml.DeprecatedClassUsageInspection;

/**
 * @author Alexey Chmutov
 *         Date: Jul 16, 2009
 *         Time: 6:11:55 PM
 */
public class TapestryHighlightingTest extends TapestryBaseTestCase {
  @Override
  protected String getBasePath() {
    return "highlighting/";
  }

  public void testTmlTagNameUsingSubpackage() throws Throwable {
    addComponentToProject("other.Count");
    doTest(true);
  }

  public void testTmlAttrName() throws Throwable {
    addComponentToProject("Count");
    doTest(true);
  }

  public void testTmlAttrNameInHtmlTag() throws Throwable {
    doTest(true, new DeprecatedClassUsageInspection());
  }

  public void testUnknownTypeOfTag() throws Throwable {
    addComponentToProject("Count");
    doTest(false);
  }

  public void testAttrNameWithUnknownPrefixInHtmlTag() throws Throwable {
    addComponentToProject("Count");
    doTest(true);
  }

  public void testTmlAttrNameWithPrefix() throws Throwable {
    addComponentToProject("Count");
    addComponentToProject("Count2");
    doTest(true, new RequiredAttributesInspection(), new TelReferencesInspection());
  }

  public void testNonPropBindingPrefix() throws Throwable {
    doTest(true);
  }

  public void testTelPropertiesAndAccessors() throws Throwable {
    doTest(true, new TelReferencesInspection());
  }

  public void testHtmlTagNameInHtmlParentTag() throws Throwable {
    addComponentToProject("Count");
    doTest(true);
  }

  public void testHtmlTagNameInHtmlParentTagError() throws Throwable {
    addComponentToProject("Count");
    doTest(true);
  }

  public void testTmlIfWithElse() throws Throwable {
    addComponentToProject("If");
    doTest(true);
  }

  protected void doTest(boolean checkInfos, LocalInspectionTool... tools) throws Throwable {
    VirtualFile templateFile = initByComponent(true);
    myFixture.enableInspections(tools);
    myFixture.testHighlighting(true, checkInfos, true, templateFile);
  }


}
