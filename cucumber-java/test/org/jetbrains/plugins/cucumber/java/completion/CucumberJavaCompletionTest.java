package org.jetbrains.plugins.cucumber.java.completion;

/**
 * User: Andrey.Vokin
 * Date: 3/14/13
 */

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.CompletionTester;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import java.io.File;

public class CucumberJavaCompletionTest extends CucumberCodeInsightTestCase {
  private CompletionTester myCompletionTester;

  public void testStepWithRegExGroups() throws Throwable {
    doTestVariants();
  }

  public void testStepWithRegex() throws Throwable {
    doTestVariants();
  }

  private void doTestVariants() throws Throwable {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myCompletionTester.doTestVariantsInner(getTestName(true) + File.separator + getTestName(true) + ".feature", GherkinFileType.INSTANCE);
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "completion" + File.separator;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCompletionTester = new CompletionTester(myFixture);
    CucumberStepsIndex.getInstance(getProject()).reset();
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return DESCRIPTOR;
  }

  public static final DefaultLightProjectDescriptor DESCRIPTOR = new DefaultLightProjectDescriptor() {
    @Override
    public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
      PsiTestUtil.addLibrary(module, model, "cucumber-java", PathManager.getHomePath() + "/community/lib", "cucumber-java-1.0.14.jar");
    }
  };
}
