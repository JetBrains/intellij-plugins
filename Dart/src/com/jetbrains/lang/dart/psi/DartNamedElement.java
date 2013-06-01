package com.jetbrains.lang.dart.psi;

import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public interface DartNamedElement extends DartPsiCompositeElement, PsiNamedElement, NavigationItem, PsiNameIdentifierOwner {
  @NotNull
  DartId getId();
}
