// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.rename;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.project.IntelliJProjectConfiguration;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

import static org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil.attachCucumberExpressionsLibrary;

/// Verify that we correctly detect and handle an attempt to rename a step definition which is in a not-writable file.
/// Such a step definition likely comes from an external JAR.
public class CucumberJavaNotWritableStepRenameTest extends BasePlatformTestCase {

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "renameStep";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        IntelliJProjectConfiguration.LibraryRoots libraryRoots =
          IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-core-5.5");
        PsiTestUtil.addProjectLibrary(model, "my-cucumber-core", libraryRoots.getClassesPaths());

        libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-java-5.5");
        PsiTestUtil.addProjectLibrary(model, "my-cucumber-java", libraryRoots.getClassesPaths());

        attachCucumberExpressionsLibrary(model);

        PsiTestUtil.addLibrary(model, "stepsJarWhoseNameDoesNotActuallyMatter", getTestDataPath() + "/notWritableStep/before",
                               "steps-1.0.0.jar");
      }
    };
  }

  public void testNotWritableStep() {
    myFixture.copyDirectoryToProject(getTestName(true) + "/before", "");
    myFixture.configureByFile("test.feature");
    assertThrows(
      () -> myFixture.renameElementAtCaretUsingHandler("newName"),
      RuntimeException.class,
      (errorMessage) -> {
        assertTrue(errorMessage.startsWith("com.intellij.util.IncorrectOperationException: File containing the step definition at "));
        assertTrue("mentions path of the file that is not writable",
                   errorMessage.contains("testData/renameStep/notWritableStep/before/steps-1.0.0.jar!/org/example/ExternalSteps.class ")
        );
        assertTrue(errorMessage.endsWith(" cannot be modified."));
      }
    );
  }
}
