package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.introduceField.JSIntroduceFieldSettings;
import com.intellij.lang.javascript.refactoring.introduceField.MockJSIntroduceFieldHandler;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableTestCase;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.testFramework.LightJavaCodeInsightTestCase;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.refactoring.introduceField.JSIntroduceFieldSettings.InitializationPlace.*;

public class FlexIntroduceFieldTest extends LightJavaCodeInsightTestCase {
  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "refactoring/introduceField/");

    super.setUp();
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("refactoring/introduceField/");
  }

  private void doTest(String varName, final String fileName, String ext) {
    doTest(varName, false, JSAttributeList.AccessType.PRIVATE, FieldDeclaration, fileName, ext);
  }

  private void doTest(String varName, final boolean replaceAll, final JSAttributeList.AccessType accessType,
                      JSIntroduceFieldSettings.InitializationPlace initializationPlace, final String fileName, String ext) {
    configureByFile(fileName + "." + ext);
    new MockJSIntroduceFieldHandler(varName, replaceAll, accessType, initializationPlace)
      .invoke(getProject(), getEditor(), getFile(), null);
    JSIntroduceVariableTestCase.waitIntroduceHandler();
    checkResultByFile(fileName + "_after." + ext);
  }

  public void testBasic() {
    doTest("created", getTestName(false), "js2");
  }

  public void testWorkingInMxml() {
    doTest("created", getTestName(false), "mxml");
  }

  public void testNoIntroduce() {
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

  public void testModifier() {
    doTest("created", true, JSAttributeList.AccessType.PROTECTED, FieldDeclaration, getTestName(false), "js2");
  }

  public void testInitializeInConstructor() {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, Constructor, getTestName(false), "js2");
  }

  public void testInitializeInConstructor2() {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, Constructor, getTestName(false), "js2");
  }

  public void testInitializeInConstructor3() {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, Constructor, getTestName(false), "js2");
  }

  public void testInitializeInCurrentMethod() {
    introduceFieldInCurrentMethod();
  }

  public void testInitializeInCurrentMethod2() {
    introduceFieldInCurrentMethod();
  }

  public void testInitializeInCurrentMethod3() {
    introduceFieldInCurrentMethod();
  }

  public void testStatic() {
    introduceFieldInCurrentMethod();
  }

  public void testIntroduceToWorkOverVar() {
    introduceFieldInCurrentMethod();
  }

  public void testIntroduceToWorkOverVar_4() {
    introduceFieldInCurrentMethod();
  }

  public void testIntroduceToWorkOverVar_2() {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, Constructor, getTestName(false), "js2");
  }

  public void testIntroduceToWorkOverVar_2_2() {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, Constructor, getTestName(false), "js2");
  }

  public void testIntroduceToWorkOverVar_3() {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, FieldDeclaration, getTestName(false), "js2");
  }

  public void testIntroduceToWorkOverVar_3_2() {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, FieldDeclaration, getTestName(false), "js2");
  }

  public void testIntroduceToWorkOverVar_5() {
    introduceFieldInCurrentMethod();
  }

  public void testIntroduceToWorkOverVar_6() {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, FieldDeclaration,
           getTestName(false), "js2");
  }

  private void introduceFieldInCurrentMethod() {
    doTest("created", true, JSAttributeList.AccessType.PRIVATE, CurrentMethod, getTestName(false), "js2");
  }
}
