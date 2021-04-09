// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.project.IntelliJProjectConfiguration;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

import static org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil.attachCucumberExpressionsLibrary;

public class CucumberJavaResolveToExternalLibraryTest extends BaseCucumberJavaResolveTest {

  public void testResolveWithParameterTypeDouble() {
    init("resolveToExternalLibrary");

    checkReference("step w<caret>ith", "stepWithDoubleParameterType");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        IntelliJProjectConfiguration.LibraryRoots
          libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-core-5.5");
        PsiTestUtil.addProjectLibrary(model, "cucumber-core", libraryRoots.getClassesPaths());

        libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-java-5.5");
        PsiTestUtil.addProjectLibrary(model, "cucumber-java", libraryRoots.getClassesPaths());

        attachCucumberExpressionsLibrary(model);

        PsiTestUtil.addLibrary(model, "cucumber-java8", PathManager.getHomePath() + "/contrib/cucumber-java/testData/resolve/resolveToExternalLibrary", "step-def-jar-1.0.jar");
      }
    };
  }
}
