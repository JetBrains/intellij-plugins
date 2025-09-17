package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.util.List;

@NotNullByDefault
public final class CucumberJavaImplicitUsageProvider implements ImplicitUsageProvider {
  @Override
  public boolean isImplicitUsage(PsiElement element) {
    if (element instanceof PsiClass psiClass) {
      return CucumberJavaUtil.isStepDefinitionClass(psiClass);
    }
    else if (element instanceof PsiMethod method) {
      if (CucumberJavaUtil.isHook(method) || CucumberJavaUtil.isParameterType(method)) return true;
      if (CucumberJavaUtil.isStepDefinition(method)) {
        final List<PsiAnnotation> stepAnnotations = CucumberJavaUtil.getCucumberStepAnnotations(method);
        for (final PsiAnnotation stepAnnotation : stepAnnotations) {
          final String regexp = CucumberJavaUtil.getPatternFromStepDefinition(stepAnnotation);
          if (regexp == null) continue;
          final Ref<@Nullable PsiReference> psiReferenceRef = new Ref<>(null);
          Processor<@Nullable PsiReference> processor = (PsiReference psiReference) -> {
            if (psiReference == null) return true;
            psiReferenceRef.set(psiReference);
            return false;
          };
          CucumberUtil.findGherkinReferencesToElement(method, regexp, processor, method.getResolveScope());
          if (psiReferenceRef.get() != null) return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    return false;
  }
}
