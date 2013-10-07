package org.jetbrains.plugins.cucumber.java;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;

/**
 * User: Andrey.Vokin
 * Date: 8/9/12
 */
public class CucumberJavaTestUtil {
  public static final String RELATED_TEST_DATA_PATH = "/contrib/cucumber-java/testData/";

  public static DefaultLightProjectDescriptor createCucumberProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
        PsiTestUtil.addLibrary(module, model, "cucumber-java", PathManager.getHomePath() + "/community/lib", "cucumber-java-1.0.14.jar");
      }
    };
  }
}
