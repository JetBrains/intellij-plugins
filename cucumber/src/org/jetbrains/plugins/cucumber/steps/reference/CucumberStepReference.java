// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.steps.reference;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepHelper;

import java.util.*;

@NotNullByDefault
public class CucumberStepReference implements PsiPolyVariantReference {
  private final PsiElement step;
  private final TextRange range;

  public CucumberStepReference(PsiElement step, TextRange range) {
    this.step = step;
    this.range = range;
  }

  @Override
  public PsiElement getElement() {
    return step;
  }

  @Override
  public TextRange getRangeInElement() {
    return range;
  }

  @Override
  public @Nullable PsiElement resolve() {
    final ResolveResult[] result = multiResolve(true);
    return result.length == 1 ? result[0].getElement() : null;
  }

  @Override
  public String getCanonicalText() {
    return step.getText();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return step;
  }

  @Override
  public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException {
    return step;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    ResolveResult[] resolvedResults = multiResolve(false);
    for (ResolveResult rr : resolvedResults) {
      if (getElement().getManager().areElementsEquivalent(rr.getElement(), element)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final Project project = getElement().getProject();
    return ResolveCache.getInstance(project).resolveWithCaching(this, MyResolver.INSTANCE, false, incompleteCode);
  }

  private ResolveResult[] multiResolveInner() {
    final Module module = ModuleUtilCore.findModuleForPsiElement(step);
    if (module == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    final List<CucumberJvmExtensionPoint> extensionPoints = CucumberJvmExtensionPoint.EP_NAME.getExtensionList();
    final Set<String> stepVariants = new HashSet<>();
    for (final CucumberJvmExtensionPoint ep : extensionPoints) {
      final String stepName = ep.getStepName(step);
      if (stepName != null) {
        stepVariants.add(stepName);
      }
    }
    if (stepVariants.isEmpty()) {
      return ResolveResult.EMPTY_ARRAY;
    }

    final Collection<AbstractStepDefinition> stepDefinitions = CucumberStepHelper.loadStepsFor(step.getContainingFile(), module);

    final List<PsiElement> resolvedElements = new ArrayList<>();
    for (final AbstractStepDefinition stepDefinition : stepDefinitions) {
      if (stepDefinition.supportsStep(step)) {
        for (String stepVariant : stepVariants) {
          final PsiElement element = stepDefinition.getElement();
          if (stepDefinition.matches(stepVariant) && element != null && !resolvedElements.contains(element)) {
            resolvedElements.add(element);
            break;
          }
        }
      }
    }

    return resolvedElements.stream().map(PsiElementResolveResult::new).toArray(ResolveResult[]::new);
  }

  public @Nullable AbstractStepDefinition resolveToDefinition() {
    final Collection<AbstractStepDefinition> definitions = resolveToDefinitions();
    return (definitions.isEmpty() ? null : definitions.iterator().next());
  }

  public Collection<AbstractStepDefinition> resolveToDefinitions() {
    return CucumberStepHelper.findStepDefinitions(step.getContainingFile(), ((GherkinStepImpl)step));
  }

  private static class MyResolver implements ResolveCache.PolyVariantResolver<CucumberStepReference> {
    private static final MyResolver INSTANCE = new MyResolver();

    @Override
    public ResolveResult[] resolve(CucumberStepReference ref, boolean incompleteCode) {
      return ref.multiResolveInner();
    }
  }
}
