package org.jetbrains.plugins.cucumber.steps.reference;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.util.ArrayUtil;
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

/**
 * @author yole
 */
public class CucumberStepReference implements PsiPolyVariantReference {

  private final PsiElement myStep;
  private final TextRange myRange;

  public CucumberStepReference(PsiElement step, TextRange range) {
    myStep = step;
    myRange = range;
  }

  public PsiElement getElement() {
    return myStep;
  }

  public TextRange getRangeInElement() {
    return myRange;
  }

  public PsiElement resolve() {
    final ResolveResult[] result = multiResolve(true);
    return result.length == 1 ? result[0].getElement() : null;
  }

  @NotNull
  public String getCanonicalText() {
    return myStep.getText();
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return myStep;
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return myStep;
  }

  public boolean isReferenceTo(PsiElement element) {
    ResolveResult[] resolvedResults = multiResolve(false);
    for (ResolveResult rr : resolvedResults) {
      if (getElement().getManager().areElementsEquivalent(rr.getElement(), element)) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public boolean isSoft() {
    return false;
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final List<ResolveResult> result = new ArrayList<>();
    final List<PsiElement> resolvedElements = new ArrayList<>();

    final CucumberJvmExtensionPoint[] extensionList = Extensions.getExtensions(CucumberJvmExtensionPoint.EP_NAME);
    for (CucumberJvmExtensionPoint e : extensionList) {
      final List<PsiElement> extensionResult = e.resolveStep(myStep);
      for (final PsiElement element : extensionResult) {
        if (element != null && !resolvedElements.contains(element)) {
          resolvedElements.add(element);
          result.add(new ResolveResult() {
            @Override
            public PsiElement getElement() {
              return element;
            }

            @Override
            public boolean isValidResult() {
              return true;
            }
          });
        }
      }
    }

    return result.toArray(new ResolveResult[result.size()]);
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
}
