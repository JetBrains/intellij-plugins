// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

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

  public void testResolveManySteps() {
    init(getTestName(true));

    checkReference("<caret>I have a normal some parameter", "normal");
    checkReference("<caret>I have this parameter", "alternative");
    checkReference("<caret>I have that parameter", "alternative");
    checkReference("<caret>I have or not parameter", "optional");
    checkReference("<caret>I have or not", "optional");
    checkReference("<caret>I have a custom blah parameter", "custom");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        IntelliJProjectConfiguration.LibraryRoots
          libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-core-5.5");
        PsiTestUtil.addProjectLibrary(model, "my-cucumber-core", libraryRoots.getClassesPaths());

        libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-java-5.5");
        PsiTestUtil.addProjectLibrary(model, "my-cucumber-java", libraryRoots.getClassesPaths());

        attachCucumberExpressionsLibrary(model);

        PsiTestUtil.addLibrary(model, "my-cucumber-java8",
                               getTestDataPath() + "/resolveManySteps",
                               "steps-1.0.0.jar");
      }
    };
  }

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/resolveToExternalLibrary";
  }
}
