package com.jetbrains.lang.dart.psi;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author: Fedor.Korotkov
 */
public class ClassNameScopeProcessor implements PsiScopeProcessor {
  private final Set<DartComponentName> result;

  public ClassNameScopeProcessor(Set<DartComponentName> result) {
    this.result = result;
  }

  @Override
  public boolean execute(@NotNull PsiElement element, ResolveState state) {
    if (element instanceof DartComponentName && element.getParent() instanceof DartClass) {
      result.add((DartComponentName)element);
    }
    return true;
  }

  @Override
  public <T> T getHint(@NotNull Key<T> hintKey) {
    return null;
  }

  @Override
  public void handleEvent(Event event, @Nullable Object associated) {
  }
}
