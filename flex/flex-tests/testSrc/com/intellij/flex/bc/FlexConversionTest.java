package com.intellij.flex.bc;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.ConversionHelper;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationManagerImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexLibraryIdGenerator;
import com.intellij.openapi.util.JDOMUtil;
import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FlexConversionTest extends ConversionTestBaseEx {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexLibraryIdGenerator.resetTestState();
  }

  //private static void createFlexSdk(ProjectJdkTable jdkTable, String name, String homePath, IFlexSdkType.Subtype type) {
  //  SdkType sdkType;
  //  if (type == IFlexSdkType.Subtype.Flex) {
  //    sdkType = FlexSdkType.getInstance();
  //  }
  //  else if (type == IFlexSdkType.Subtype.AIR) {
  //    sdkType = AirSdkType.getInstance();
  //  }
  //  else if (type == IFlexSdkType.Subtype.AIRMobile) {
  //    sdkType = AirMobileSdkType.getInstance();
  //  }
  //  else {
  //    throw new IllegalArgumentException(type.toString());
  //  }
  //
  //  Sdk flexSdk1 = jdkTable.createSdk(name, sdkType);
  //  jdkTable.addJdk(flexSdk1);
  //  SdkModificator modificator = flexSdk1.getSdkModificator();
  //  modificator.setHomePath(homePath);
  //  modificator.commitChanges();
  //}


  @Override
  protected String getHomePath() {
    return FlexTestUtils.getTestDataPath("conversion/");
  }

  @Override
  protected String getBasePath() {
    return "";
  }

  public void testFacets() throws IOException, JDOMException {
    doTest(getTestName(false), true);
  }

  public void testJavaProject() throws IOException, JDOMException {
    doTest(getTestName(false), false);
  }

  public void testModuleLibraries() throws IOException, JDOMException {
    doTest(getTestName(false), true);
  }

  public void testProjectLibraries() throws IOException, JDOMException {
    doTest(getTestName(false), true);
  }

  public void testUniqueNames() {
    doTestUniqueNames(new String[]{}, new String[]{});
    doTestUniqueNames(new String[]{"a", "b", "c", "a"}, new String[]{"a", "b", "c", "a (1)"});
    doTestUniqueNames(new String[]{"a", "b", "c", "a", "a"}, new String[]{"a", "b", "c", "a (1)", "a (2)"});
    doTestUniqueNames(new String[]{"a", "b", "c", "a", "a (1)", "a (2)"}, new String[]{"a", "b", "c", "a (3)", "a (1)", "a (2)"});
  }

  private static void doTestUniqueNames(String[] input, String[] output) {
    List<String> result = FlexBuildConfigurationManagerImpl.generateUniqueNames(Arrays.asList(input));
    TestCase.assertTrue("output: " + Arrays.toString(result.toArray()) + ", expected: " + Arrays.toString(output),
                        result.equals(Arrays.asList(output)));
  }

  @SuppressWarnings("UnusedDeclaration")
  private static String toString(Element e) {
    Element clone = e.clone();
    ConversionHelper.collapsePaths(clone);
    return JDOMUtil.writeDocument(new Document(clone), "\n");
  }
}
