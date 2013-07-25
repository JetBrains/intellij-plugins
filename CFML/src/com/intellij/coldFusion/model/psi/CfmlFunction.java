package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.info.CfmlFunctionDescription;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CfmlFunction extends PsiNameIdentifierOwner {
  CfmlFunction[] EMPTY_ARRAY = new CfmlFunction[0];

  @NotNull
  String getParametersAsString();

  @NotNull
  CfmlParameter[] getParameters();

  @Nullable
  PsiType getReturnType();

  @NotNull
  String getName();

  @NotNull
  CfmlFunctionDescription getFunctionInfo();
}
