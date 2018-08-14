package com.jetbrains.lang.dart.ide.findUsages;

import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartLibraryNameElement;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class DartFindUsagesProvider implements FindUsagesProvider {
  @Override
  public WordsScanner getWordsScanner() {
    return null;
  }

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return psiElement instanceof PsiNamedElement;
  }

  @Override
  public String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @Override
  @NotNull
  public String getType(@NotNull final PsiElement element) {
    if (element instanceof DartLibraryNameElement) {
      return "library";
    }
    final DartComponentType type = DartComponentType.typeOf(element.getParent());
    return type == null ? "reference" : type.toString().toLowerCase(Locale.US);
  }

  @Override
  @NotNull
  public String getDescriptiveName(@NotNull final PsiElement element) {
    if (element instanceof PsiNamedElement) {
      return StringUtil.notNullize(((PsiNamedElement)element).getName());
    }
    return "";
  }

  @Override
  @NotNull
  public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
    final String name = element instanceof PsiNamedElement ? ((PsiNamedElement)element).getName() : null;
    return name != null ? name : element.getText();
  }
}
