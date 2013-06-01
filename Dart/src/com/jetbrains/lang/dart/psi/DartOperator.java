package com.jetbrains.lang.dart.psi;

import com.intellij.navigation.NavigationItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public interface DartOperator extends DartPsiCompositeElement, NavigationItem {
  @Nullable
  DartFormalParameterList getFormalParameterList();

  @Nullable
  DartReturnType getReturnType();

  @Nullable
  DartUserDefinableOperator getUserDefinableOperator();

  @NotNull
  String getOperatorString();
}
