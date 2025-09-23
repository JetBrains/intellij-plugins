package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.codeInspection.deadCode.UnusedDeclarationInspectionBase;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.resolve.BaseCucumberJavaResolveTest;

/// We extend [BaseCucumberJavaResolveTest] because we need to be able to resolve
/// between Cucumber feature files and step definitions in Java to test the inspection.
public class UnusedJavaMethodInspectionTest extends BaseCucumberJavaResolveTest {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.allowTreeAccessForAllFiles();
  }

  protected void doTest(String file) {
    myFixture.enableInspections(new UnusedDeclarationInspectionBase(true));
    myFixture.copyDirectoryToProject(".", "");
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir(file));
    myFixture.testHighlighting();
  }

  public void testStepDefinition() {
    doTest("ShoppingStepdefs.java");
  }

  public void testPrivateMethod() {
    doTest("PrivateMethod.java");
  }

  public void testHooks() {
    doTest("Hooks.java");
  }

  @Override
  protected String getRelatedTestDataPath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "inspections/unusedMethod";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber7ProjectDescriptor();
  }
}
