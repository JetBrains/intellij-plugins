package org.jetbrains.plugins.cucumber.java;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

public class CucumberJavaTestUtil {
  public static final String RELATED_TEST_DATA_PATH = "/contrib/cucumber-java/testData/";

  public static DefaultLightProjectDescriptor createCucumberProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        attachCucumberLibraries(module, model);
      }
    };
  }

  public static DefaultLightProjectDescriptor createCucumberJava8ProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        attachCucumberLibraries(module, model);

        LanguageLevelModuleExtension extension = model.getModuleExtension(LanguageLevelModuleExtension.class);
        if (extension != null) {
          extension.setLanguageLevel(LanguageLevel.JDK_1_8);
        }

        VirtualFile sourceRoot = VirtualFileManager.getInstance().refreshAndFindFileByUrl("temp:///src");
        if (sourceRoot != null) {
          contentEntry.removeSourceFolder(contentEntry.getSourceFolders()[0]);
          contentEntry.addSourceFolder(sourceRoot, true);
        }
      }
    };
  }

  protected static void attachCucumberLibraries(@NotNull Module module, @NotNull ModifiableRootModel model) {
    PsiTestUtil.addLibrary(module, model, "cucumber-java", PathManager.getHomePath() + "/community/lib", "cucumber-java-1.2.4.jar");
    PsiTestUtil.addLibrary(module, model, "cucumber-core", PathManager.getHomePath() + "/community/lib", "cucumber-core-1.2.4.jar");
    PsiTestUtil.addLibrary(module, model, "cucumber-java8", PathManager.getHomePath() + "/community/lib", "cucumber-java8-1.2.4.jar");
    PsiTestUtil.addLibrary(module, model, "cucumber-jvm-deps", PathManager.getHomePath() + "/community/lib", "cucumber-jvm-deps-1.0.3.jar");
  }
}
