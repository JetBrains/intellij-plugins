package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;

public class GherkinFindUsagesProvider implements FindUsagesProvider {
  @Override
  public WordsScanner getWordsScanner() {
    return new DefaultWordsScanner(new GherkinLexer(new PlainGherkinKeywordProvider()), TokenSet.EMPTY, TokenSet.EMPTY, TokenSet.EMPTY);
  }

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    if (psiElement instanceof GherkinStep) {
      return true;
    }

    return false;
  }

  @Override
  public String getHelpId(@NotNull PsiElement psiElement) {
    return "reference.dialogs.findUsages.other";
  }

  @NotNull
  @Override
  public String getType(@NotNull PsiElement element) {
    if (element instanceof GherkinStep) {
      return CucumberBundle.message("cucumber.step");
    } else if (element instanceof GherkinStepParameter) {
      return CucumberBundle.message("cucumber.step.parameter");
    }
    return element.toString();
  }

  @NotNull
  @Override
  public String getDescriptiveName(@NotNull PsiElement element) {
    return element instanceof PsiNamedElement ? ((PsiNamedElement)element).getName() : "";
  }

  @NotNull
  @Override
  public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
    return getDescriptiveName(element);
  }
}
