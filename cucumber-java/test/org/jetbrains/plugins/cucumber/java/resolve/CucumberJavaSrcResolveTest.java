package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;

/**
 * User: Andrey.Vokin
 * Date: 3/4/13
 */
public class CucumberJavaSrcResolveTest extends BaseCucumberJavaResolveTest {
  public void testNavigationToSrc() throws Exception {
    doTest("stepResolve_03", "tes<caret>t \"test\"", "test");
  }

  public void testResolveToStepWithStringConcatenation() throws Exception {
    doTest("stepResolveStringConcatenation", "subt<caret>ract", "I_subtract_from");
  }

  public void testResolveToStepWithTimeout() throws Exception {
    doTest("resolveToStepWithTimeout", "subt<caret>ract", "I_subtract_from");
  }

  public void testStrictStartAndEndRegexOptions() throws Exception {
    doTest("strictStartAndEndRegexOptions", "I have sh<caret>ort step", "I_have_short_step");
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
