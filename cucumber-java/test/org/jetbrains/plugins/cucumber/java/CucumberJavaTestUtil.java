package org.jetbrains.plugins.cucumber.java;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.project.IntelliJProjectConfiguration;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

public class CucumberJavaTestUtil {
  public static final String RELATED_TEST_DATA_PATH = "/contrib/cucumber-java/testData/";

  public static DefaultLightProjectDescriptor createCucumberProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        attachCucumberCore2(model);
        attachStandardCucumberLibraries(module, model);
      }
    };
  }

  public static DefaultLightProjectDescriptor createCucumber3ProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        attachCucumberCore3(model);
        attachStandardCucumberLibraries(module, model);
      }
    };
  }

  public static DefaultLightProjectDescriptor createCucumberJava8ProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        attachCucumberCore2(model);
        attachStandardCucumberLibraries(module, model);

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

  protected static void attachStandardCucumberLibraries(@NotNull Module module, @NotNull ModifiableRootModel model) {
    PsiTestUtil.addProjectLibrary(model, "cucumber-java", IntelliJProjectConfiguration.getProjectLibraryClassesRootPaths("cucumber-java"));
    PsiTestUtil.addProjectLibrary(model, "cucumber-jvm-deps", IntelliJProjectConfiguration.getProjectLibraryClassesRootPaths("cucumber-testing"));
    PsiTestUtil.addLibrary(module, model, "cucumber-java8", PathManager.getHomePath() + "/community/lib", "cucumber-java8-1.2.4.jar");
  }
  
  protected static void attachCucumberCore2(@NotNull ModifiableRootModel model) {
    PsiTestUtil.addProjectLibrary(model, "cucumber-core", IntelliJProjectConfiguration.getProjectLibraryClassesRootPaths("cucumber-core"));
  }

  private static void attachCucumberCore3(@NotNull ModifiableRootModel model) {
    IntelliJProjectConfiguration.LibraryRoots
      libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-core-3");
    PsiTestUtil.addProjectLibrary(model, "cucumber-core", libraryRoots.getClassesPaths());

    libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-expressions");
    PsiTestUtil.addProjectLibrary(model, "cucumber-expressions", libraryRoots.getClassesPaths());
  }
}
