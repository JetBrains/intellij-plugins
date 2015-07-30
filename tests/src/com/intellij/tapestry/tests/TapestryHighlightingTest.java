package com.intellij.tapestry.tests;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.codeInspection.xml.DeprecatedClassUsageInspection;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.tapestry.intellij.inspections.TelReferencesInspection;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.util.containers.ContainerUtil;

import java.util.Set;

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
  private static final Set<String> ourTestsWithExtraLibraryComponents = ContainerUtil.newHashSet("ComponentFromJar", "LibraryMapping");

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

  public void testHtml5() throws Throwable {
    doTest(true);
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

  public void testTelPropertiesAndAccessors2() throws Throwable {
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
    addComponentToProject("TestComp");
    doTest(true);
  }

  public void testAbstractComponent() throws Throwable {
    addAbstractComponentToProject("AbstractComponent");
    final String tmlName = getElementTemplateFileName();
    final VirtualFile templateFile = myFixture.copyFileToProject(tmlName, ABSTRACT_COMPONENTS_PACKAGE_PATH + tmlName);
    myFixture.configureFromExistingVirtualFile(templateFile);
    myFixture.enableInspections(new TelReferencesInspection());
    myFixture.testHighlighting(true, true, true, templateFile);
  }

  public void testComponentFromJar() throws Throwable {
    doTest(false);
  }

  public void testLibraryMapping() throws Throwable {
    addComponentToProject("Count3");
    doTest(false);
  }

  public void testNewSchema() throws Throwable {
    doTest(true);
  }

  @Override
  protected void addTapestryLibraries(JavaModuleFixtureBuilder moduleBuilder) {
    super.addTapestryLibraries(moduleBuilder);
    if (ourTestsWithExtraLibraryComponents.contains(getTestName(false))) {
      moduleBuilder.addLibraryJars("tapestry_5.1.0.5_additional", Util.getCommonTestDataPath() + "libs", "tapestry-upload-5.1.0.5.jar");
    }
  }

  protected void doTest(boolean checkInfos, LocalInspectionTool... tools) throws Throwable {
    VirtualFile templateFile = initByComponent(true);
    myFixture.enableInspections(tools);
    myFixture.testHighlighting(true, checkInfos, true, templateFile);
  }
}
