// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.steps.reference;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yole
 */
public class CucumberStepReference implements PsiPolyVariantReference {
  private static final MyResolver RESOLVER = new MyResolver();

  private final PsiElement myStep;
  private final TextRange myRange;

  public CucumberStepReference(PsiElement step, TextRange range) {
    myStep = step;
    myRange = range;
  }

  @Override
  @NotNull
  public PsiElement getElement() {
    return myStep;
  }

  @Override
  @NotNull
  public TextRange getRangeInElement() {
    return myRange;
  }

  @Override
  public PsiElement resolve() {
    final ResolveResult[] result = multiResolve(true);
    return result.length == 1 ? result[0].getElement() : null;
  }

  @Override
  @NotNull
  public String getCanonicalText() {
    return myStep.getText();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    return myStep;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return myStep;
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
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

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    Project project = getElement().getProject();
    return ResolveCache.getInstance(project).resolveWithCaching(this, RESOLVER, false, incompleteCode);
  }

  private ResolveResult[] multiResolveInner() {
    final Module module = ModuleUtilCore.findModuleForPsiElement(myStep);
    if (module == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    List<CucumberJvmExtensionPoint> frameworks = CucumberJvmExtensionPoint.EP_NAME.getExtensionList();
    Collection<String> stepVariants =
      frameworks.stream().map(e -> e.getStepName(myStep)).filter(Objects::nonNull).collect(Collectors.toSet());
    if (stepVariants.size() == 0) {
      return ResolveResult.EMPTY_ARRAY;
    }

    PsiFile featureFile = myStep.getContainingFile();
    List<AbstractStepDefinition> stepDefinitions = CachedValuesManager.getCachedValue(featureFile, () -> {
      List<AbstractStepDefinition> allStepDefinition = new ArrayList<>();
      for (CucumberJvmExtensionPoint e : frameworks) {
        allStepDefinition.addAll(e.loadStepsFor(featureFile, module));
      }
      return CachedValueProvider.Result.create(allStepDefinition, PsiModificationTracker.MODIFICATION_COUNT);
    });
    
    List<PsiElement> resolvedElements = new ArrayList<>();
    for (final AbstractStepDefinition stepDefinition : stepDefinitions) {
      if (stepDefinition.supportsStep(myStep)) {
        for (String stepVariant : stepVariants) {
          PsiElement element = stepDefinition.getElement();
          if (stepDefinition.matches(stepVariant) && element != null && !resolvedElements.contains(element)) {
            resolvedElements.add(element);
            break;
          }
        }
      }
    }

    return resolvedElements.stream().map(e -> new ResolveResult() {
      @Override
      public PsiElement getElement() {
        return e;
      }

      @Override
      public boolean isValidResult() {
        return true;
      }
    }).toArray(ResolveResult[]::new);
  }

  /**
   * @return first definition (if any) or null if no definition found
   * @see #resolveToDefinitions()
   */
  @Nullable
  public AbstractStepDefinition resolveToDefinition() {
    final Collection<AbstractStepDefinition> definitions = resolveToDefinitions();
    return (definitions.isEmpty() ? null : definitions.iterator().next());
  }

  /**
   * @return step definitions
   * @see #resolveToDefinition()
   */
  @NotNull
  public Collection<AbstractStepDefinition> resolveToDefinitions() {
    final CucumberStepsIndex index = CucumberStepsIndex.getInstance(myStep.getProject());
    return index.findStepDefinitions(myStep.getContainingFile(), ((GherkinStepImpl)myStep));
  }

  private static class MyResolver implements ResolveCache.PolyVariantResolver<CucumberStepReference> {
    @Override
    @NotNull
    public ResolveResult[] resolve(@NotNull CucumberStepReference ref, boolean incompleteCode) {
      return ref.multiResolveInner();
    }
  }
}
