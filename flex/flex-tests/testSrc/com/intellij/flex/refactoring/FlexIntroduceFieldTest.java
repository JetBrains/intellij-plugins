package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.introduceField.JSIntroduceFieldSettings;
import com.intellij.lang.javascript.refactoring.introduceField.MockJSIntroduceFieldHandler;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.testFramework.LightCodeInsightTestCase;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.refactoring.introduceField.JSIntroduceFieldSettings.InitializationPlace.*;
import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexIntroduceFieldTest extends LightCodeInsightTestCase {
  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("refactoring/introduceField/");
  }

  private void doTest(String varName, final String fileName, String ext) throws Exception {
    doTest(varName, false, JSAttributeList.AccessType.PRIVATE, FieldDeclaration, fileName, ext);
  }

  private void doTest(String varName, final boolean replaceAll, final JSAttributeList.AccessType accessType,
                      JSIntroduceFieldSettings.InitializationPlace initializationPlace, final String fileName, String ext)
    throws Exception {
    configureByFile(fileName + "." + ext);
    new MockJSIntroduceFieldHandler(varName, replaceAll, accessType, initializationPlace)
      .invoke(getProject(), getEditor(), getFile(), null);
    checkResultByFile(fileName + "_after." + ext);
  }

  public void testBasic() throws Exception {
    doTest("created", getTestName(false), "js2");
  }

  public void testWorkingInMxml() throws Exception {
    doTest("created", getTestName(false), "mxml");
  }

  public void testNoIntroduce() throws Exception {
    String testName = getTestName(false);
    configureByFile(testName + ".js2");
    try {
      new MockJSIntroduceFieldHandler("foo", false, JSAttributeList.AccessType.PACKAGE_LOCAL, CurrentMethod).invoke(getProject(),
                                                                                                                    getEditor(), getFile(),
                                                                                                                    null);
      assertTrue(false);
    }
    catch (CommonRefactoringUtil.RefactoringErrorHintException e) {
      // ok
    }
  }

  public void testModifier() throws Exception {
    doTest("created", true, JSAttributeList.AccessType.PROTECTED, FieldDeclaration, getTestName(false), "js2");
  }

  public void testInitializeInConstructor() throws Exception {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, Constructor, getTestName(false), "js2");
  }

  public void testInitializeInConstructor2() throws Exception {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, Constructor, getTestName(false), "js2");
  }

  public void testInitializeInConstructor3() throws Exception {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, Constructor, getTestName(false), "js2");
  }

  public void testInitializeInCurrentMethod() throws Exception {
    introduceFieldInCurrentMethod();
  }

  public void testInitializeInCurrentMethod2() throws Exception {
    introduceFieldInCurrentMethod();
  }

  public void testInitializeInCurrentMethod3() throws Exception {
    introduceFieldInCurrentMethod();
  }

  public void testStatic() throws Exception {
    introduceFieldInCurrentMethod();
  }

  public void testIntroduceToWorkOverVar() throws Exception {
    introduceFieldInCurrentMethod();
  }

  public void testIntroduceToWorkOverVar_4() throws Exception {
    introduceFieldInCurrentMethod();
  }

  public void testIntroduceToWorkOverVar_2() throws Exception {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, Constructor, getTestName(false), "js2");
  }

  public void testIntroduceToWorkOverVar_2_2() throws Exception {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, Constructor, getTestName(false), "js2");
  }

  public void testIntroduceToWorkOverVar_3() throws Exception {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, FieldDeclaration, getTestName(false), "js2");
  }

  public void testIntroduceToWorkOverVar_3_2() throws Exception {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, FieldDeclaration, getTestName(false), "js2");
  }

  public void testIntroduceToWorkOverVar_5() throws Exception {
    introduceFieldInCurrentMethod();
  }

  public void testIntroduceToWorkOverVar_6() throws Exception {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, FieldDeclaration,
           getTestName(false), "js2");
  }

  private void introduceFieldInCurrentMethod() throws Exception {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, CurrentMethod, getTestName(false), "js2");
  }
}
