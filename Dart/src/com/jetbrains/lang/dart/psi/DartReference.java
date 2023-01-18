package com.jetbrains.lang.dart.psi;

import com.intellij.psi.PsiReference;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import org.jetbrains.annotations.NotNull;

public interface DartReference extends DartExpression, PsiReference {
  @NotNull
  DartClassResolveResult resolveDartClass();
}
