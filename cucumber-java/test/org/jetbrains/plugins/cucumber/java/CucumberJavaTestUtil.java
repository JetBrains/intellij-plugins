// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.project.IntelliJProjectConfiguration;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.jetbrains.plugins.cucumber.java.CucumberJavaVersionUtil.CUCUMBER_CORE_VERSION_5;

public final class CucumberJavaTestUtil {
  public static final String RELATED_TEST_DATA_PATH = "/contrib/cucumber-java/testData/";

  public static DefaultLightProjectDescriptor createCucumber1ProjectDescriptor() {
    return new DefaultLightProjectDescriptor(IdeaTestUtil::getMockJdk17) {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        attachCucumberCore1(model);
        attachStandardCucumberLibraries(model);
      }
    };
  }

  public static DefaultLightProjectDescriptor createCucumber2ProjectDescriptor() {
    return new DefaultLightProjectDescriptor(IdeaTestUtil::getMockJdk11) {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        attachCucumberCore2(model);
        attachStandardCucumberLibraries(model);
      }
    };
  }

  public static DefaultLightProjectDescriptor createCucumber3ProjectDescriptor() {
    return new DefaultLightProjectDescriptor(IdeaTestUtil::getMockJdk11) {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        attachCucumberCore3(model);
        attachStandardCucumberLibraries(model);
      }
    };
  }

  public static DefaultLightProjectDescriptor createCucumber4_5ProjectDescriptor() {
    return createCucumberProjectDescriptor("4.5");
  }

  public static DefaultLightProjectDescriptor createCucumber5ProjectDescriptor() {
    return createCucumberProjectDescriptor("5");
  }

  public static DefaultLightProjectDescriptor createCucumber5_5ProjectDescriptor() {
    return createCucumberProjectDescriptor("5.5");
  }

  public static DefaultLightProjectDescriptor createCucumber7ProjectDescriptor() {
    return createCucumberProjectDescriptor("7");
  }

  private static DefaultLightProjectDescriptor createCucumberProjectDescriptor(@NotNull String version) {
    return new DefaultLightProjectDescriptor(IdeaTestUtil::getMockJdk11) {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        IntelliJProjectConfiguration.LibraryRoots libraryRoots;
        libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-core-" + version);
        PsiTestUtil.addProjectLibrary(model, "cucumber-core", libraryRoots.getClassesPaths());

        libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-java-" + version);
        PsiTestUtil.addProjectLibrary(model, "cucumber-java", libraryRoots.getClassesPaths());

        if (VersionComparatorUtil.compare(version, CUCUMBER_CORE_VERSION_5) >= 0) {
          libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-java8-" + version);
          PsiTestUtil.addProjectLibrary(model, "cucumber-java8", libraryRoots.getClassesPaths());
        }

        attachCucumberExpressionsLibrary(model);
      }
    };
  }

  public static DefaultLightProjectDescriptor createCucumberJava8ProjectDescriptor() {
    return new DefaultLightProjectDescriptor(IdeaTestUtil::getMockJdk11) {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        attachCucumberCore2(model);
        attachStandardCucumberLibraries(model);

        LanguageLevelModuleExtension extension = model.getModuleExtension(LanguageLevelModuleExtension.class);
        if (extension != null) {
          extension.setLanguageLevel(LanguageLevel.JDK_11);
        }

        VirtualFile sourceRoot = VirtualFileManager.getInstance().refreshAndFindFileByUrl("temp:///src");
        if (sourceRoot != null) {
          contentEntry.removeSourceFolder(contentEntry.getSourceFolders()[0]);
          contentEntry.addSourceFolder(sourceRoot, true);
        }
      }
    };
  }

  public static void attachStandardCucumberLibraries(@NotNull ModifiableRootModel model) {
    IntelliJProjectConfiguration.LibraryRoots libraryRoots;
    libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-java");
    PsiTestUtil.addProjectLibrary(model, "cucumber-java", libraryRoots.getClassesPaths());

    PsiTestUtil.addProjectLibrary(model, "cucumber-jvm-deps",
                                  IntelliJProjectConfiguration.getProjectLibraryClassesRootPaths("cucumber-testing"));
  }

  private static void attachCucumberCore1(@NotNull ModifiableRootModel model) {
    PsiTestUtil.addProjectLibrary(model, "cucumber-core",
                                  IntelliJProjectConfiguration.getProjectLibraryClassesRootPaths("cucumber-core-1"));
  }

  private static void attachCucumberCore2(@NotNull ModifiableRootModel model) {
    List<String> libraryClassesRootPaths = IntelliJProjectConfiguration.getProjectLibraryClassesRootPaths("cucumber-core-2");
    PsiTestUtil.addProjectLibrary(model, "cucumber-core", libraryClassesRootPaths);
  }

  public static void attachCucumberCore3(@NotNull ModifiableRootModel model) {
    IntelliJProjectConfiguration.LibraryRoots libraryRoots;
    libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-core-3");
    PsiTestUtil.addProjectLibrary(model, "cucumber-core", libraryRoots.getClassesPaths());

    attachCucumberExpressionsLibrary(model);
  }

  public static void attachCucumberExpressionsLibrary(@NotNull ModifiableRootModel model) {
    IntelliJProjectConfiguration.LibraryRoots libraryRoots;
    libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-expressions");
    PsiTestUtil.addProjectLibrary(model, "cucumber-expressions", libraryRoots.getClassesPaths());
  }
}
