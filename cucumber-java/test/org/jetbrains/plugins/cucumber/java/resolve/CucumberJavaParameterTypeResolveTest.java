// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.PomTargetPsiElementImpl;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaParameterTypeReference;

public class CucumberJavaParameterTypeResolveTest extends BaseCucumberJavaResolveTest {
  public void testParameterTypeResolve() {
    init("stepResolve_ParameterType", "ParameterTypeSteps.java");

    checkReference("{iso-<caret>date}", "iso-date");
  }

  public void testNoReferenceInStringLiteralOutsideAnnotations() {
    init("stepResolve_ParameterType", "ParameterTypeSteps.java");
    final PsiReference reference = findReferenceBySignature("System.out.println(\"te<caret>xt\")");
    assertFalse("CucumberJavaParameterTypeReference not expected inside method body",
                reference instanceof CucumberJavaParameterTypeReference);
  }

  public void testResolveEnumParameterType() {
    init("stepResolve_ParameterType", "ParameterTypeSteps.java");

    checkReference("{MyEnu<caret>m}", "MyEnum");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber4_5ProjectDescriptor();
  }

  @Nullable
  @Override
  protected String getStepDefinitionName(@NotNull final PsiElement element) {
    if (element instanceof PomTargetPsiElementImpl) {
      return ((PomTargetPsiElementImpl)element).getName();
    }
    return null;
  }
}
