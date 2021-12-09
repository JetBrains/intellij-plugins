package com.intellij.tapestry.tests;

import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.codeInspection.xml.DeprecatedClassUsageInspection;
import com.intellij.lang.properties.codeInspection.unused.UnusedPropertyInspection;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.tapestry.intellij.inspections.TelReferencesInspection;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.util.containers.ContainerUtil;

import java.util.Set;

import static com.intellij.codeInsight.daemon.impl.HighlightInfoFilter.EXTENSION_POINT_NAME;

/**
 * @author Alexey Chmutov
 */
public class TapestryHighlightingTest extends TapestryBaseTestCase {
  private static final Set<String> ourTestsWithExtraLibraryComponents = ContainerUtil.newHashSet("ComponentFromJar", "LibraryMapping");

  @Override
  protected String getBasePath() {
    return "highlighting/";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    suppressXmlNSAnnotator();
  }

  private void suppressXmlNSAnnotator() {
    HighlightInfoFilter filter = (info, file) -> info.forcedTextAttributesKey != XmlHighlighterColors.XML_NS_PREFIX;
    EXTENSION_POINT_NAME.getPoint().registerExtension(filter, myFixture.getTestRootDisposable());
  }

  public void testTmlTagNameUsingSubpackage() {
    addComponentToProject("other.Count");
    ExpectedHighlightingData.expectedDuplicatedHighlighting(() -> doTest(true));
  }

  public void testTmlAttrName() {
    addComponentToProject("Count");
    ExpectedHighlightingData.expectedDuplicatedHighlighting(() -> doTest(true));
  }

  public void testTmlAttrNameInHtmlTag() {
    ExpectedHighlightingData.expectedDuplicatedHighlighting(() -> doTest(true, new DeprecatedClassUsageInspection()));
  }

  public void testHtml5() {
    doTest(true);
  }

  public void testUnknownTypeOfTag() {
    addComponentToProject("Count");
    doTest(false);
  }

  public void testAttrNameWithUnknownPrefixInHtmlTag() {
    addComponentToProject("Count");
    doTest(true);
  }

  public void testTmlAttrNameWithPrefix() {
    addComponentToProject("Count");
    addComponentToProject("Count2");
    doTest(false, new RequiredAttributesInspection(), new TelReferencesInspection());
  }

  public void testNonPropBindingPrefix() {
    doTest(true);
  }

  public void testTelPropertiesAndAccessors() {
    doTest(true, new TelReferencesInspection());
  }

  public void testTelPropertiesAndAccessors2() {
    doTest(true, new TelReferencesInspection());
  }

  public void testHtmlTagNameInHtmlParentTag() {
    addComponentToProject("Count");
    ExpectedHighlightingData.expectedDuplicatedHighlighting(() -> doTest(true));
  }

  public void testHtmlTagNameInHtmlParentTagError() {
    addComponentToProject("Count");
    doTest(true);
  }

  public void testTmlIfWithElse() {
    addComponentToProject("If");
    addComponentToProject("TestComp");
    ExpectedHighlightingData.expectedDuplicatedHighlighting(() -> doTest(true));
  }

  public void testAbstractComponent() {
    addAbstractComponentToProject("AbstractComponent");
    final String tmlName = getElementTemplateFileName();
    final VirtualFile templateFile = myFixture.copyFileToProject(tmlName, ABSTRACT_COMPONENTS_PACKAGE_PATH + tmlName);
    myFixture.configureFromExistingVirtualFile(templateFile);
    myFixture.enableInspections(new TelReferencesInspection());
    myFixture.testHighlighting(true, true, true, templateFile);
  }

  public void testComponentFromJar() {
    doTest(false);
  }

  public void testLibraryMapping() {
    addComponentToProject("Count3");
    doTest(false);
  }

  public void testNewSchema() {
    ExpectedHighlightingData.expectedDuplicatedHighlighting(() -> doTest(true));
  }

  public void testPropertyReferences() {
    myFixture.enableInspections(new UnusedPropertyInspection());
    myFixture.testHighlighting(true, true, true, getTestName(false) + ".properties", getTestName(false) + ".tml");
  }

  public void testSchema() {
    doTest(false);
  }

  @Override
  protected void addTapestryLibraries(JavaModuleFixtureBuilder moduleBuilder) {
    super.addTapestryLibraries(moduleBuilder);
    if (ourTestsWithExtraLibraryComponents.contains(getTestName(false))) {
      moduleBuilder.addLibraryJars("tapestry_5.1.0.5_additional", Util.getCommonTestDataPath() + "libs", "tapestry-upload-5.1.0.5.jar");
    }
  }

  protected void doTest(boolean checkInfos, LocalInspectionTool... tools) {
    VirtualFile templateFile = initByComponent(true);
    myFixture.enableInspections(tools);
    myFixture.testHighlighting(true, checkInfos, true, templateFile);
  }
}
