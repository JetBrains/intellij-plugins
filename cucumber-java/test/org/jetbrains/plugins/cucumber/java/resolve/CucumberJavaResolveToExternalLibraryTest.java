// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.project.IntelliJProjectConfiguration;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;
import static org.jetbrains.plugins.cucumber.java.resolve.CucumberJavaResolveFixtures.cucumberJavaResolveFixture;

@TestApplication
public class CucumberJavaResolveToExternalLibraryTest {
  private static final String RELATED_TEST_DATA_PATH =
    CucumberJavaResolveFixtures.RESOLVE_RELATED_TEST_DATA_PATH + "/resolveToExternalLibrary";

  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture(createDescriptor());
  private final TestFixture<CucumberResolveFixture> cucumber = cucumberJavaResolveFixture(projectFixture, RELATED_TEST_DATA_PATH);

  @Test
  public void testResolveManySteps() {
    CucumberResolveFixture resolve = cucumber.get();
    resolve.init(resolve.getTestName());

    resolve.checkReference("<caret>I have a normal some parameter", "normal");
    resolve.checkReference("<caret>I have this parameter", "alternative");
    resolve.checkReference("<caret>I have that parameter", "alternative");
    resolve.checkReference("<caret>I have or not parameter", "optional");
    resolve.checkReference("<caret>I have or not", "optional");
    resolve.checkReference("<caret>I have a custom blah parameter", "custom");
    resolve.checkReference("<caret>I have multiple custom blah parameters - this is first", "customMultiple");
    resolve.checkReference("<caret>I have multiple custom blah parameters - this is second", "customMultiple");
  }

  private static DefaultLightProjectDescriptor createDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        super.configureModule(module, model, contentEntry);
        IntelliJProjectConfiguration.LibraryRoots
          libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java.tests", "cucumber-core-5.5");
        PsiTestUtil.addProjectLibrary(model, "my-cucumber-core", libraryRoots.getClassesPaths());

        libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java.tests", "cucumber-java-5.5");
        PsiTestUtil.addProjectLibrary(model, "my-cucumber-java", libraryRoots.getClassesPaths());

        CucumberJavaTestUtil.attachCucumberExpressionsLibrary(model);

        PsiTestUtil.addLibrary(model, "my-cucumber-java8",
                               PathManager.getHomePath() + RELATED_TEST_DATA_PATH + "/resolveManySteps",
                               "steps-1.0.0.jar");
      }
    };
  }
}
