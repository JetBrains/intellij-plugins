// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.PomTargetPsiElementImpl;
import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaParameterTypeReference;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;
import static org.jetbrains.plugins.cucumber.java.resolve.CucumberJavaResolveFixtures.cucumberJavaResolveFixture;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestApplication
public class CucumberJavaParameterTypeResolveTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture(CucumberJavaTestUtil.createCucumber4_8ProjectDescriptor());
  private final TestFixture<CucumberResolveFixture> cucumber = cucumberJavaResolveFixture(
    projectFixture,
    CucumberJavaResolveFixtures.RESOLVE_RELATED_TEST_DATA_PATH,
    element -> getStepDefinitionName(element)
  );

  @Test
  public void testHighlightingOK() {
    cucumber.get().init("stepResolve_ParameterType", "ParameterTypeSteps.java");
    cucumber.get().getFixture().testHighlighting("ParameterTypeSteps.java");
  }

  @Test
  public void testParameterTypeResolve() {
    cucumber.get().init("stepResolve_ParameterType", "ParameterTypeSteps.java");

    cucumber.get().checkReference("{iso-<caret>date}", "iso-date");
  }

  @Test
  public void testNoReferenceInStringLiteralOutsideAnnotations() {
    cucumber.get().init("stepResolve_ParameterType", "ParameterTypeSteps.java");
    final PsiReference reference = cucumber.get().findReferenceBySignature("System.out.println(\"te<caret>xt\")");
    assertFalse(reference instanceof CucumberJavaParameterTypeReference,
                "CucumberJavaParameterTypeReference not expected inside method body");
  }

  @Test
  public void testResolveEnumParameterType() {
    cucumber.get().init("stepResolve_ParameterType", "ParameterTypeSteps.java");

    cucumber.get().checkReference("{MyEnu<caret>m}", "MyEnum");
  }

  private static @Nullable String getStepDefinitionName(@NotNull final PsiElement element) {
    if (element instanceof PomTargetPsiElementImpl psiElement) {
      return psiElement.getName();
    }
    return null;
  }
}
