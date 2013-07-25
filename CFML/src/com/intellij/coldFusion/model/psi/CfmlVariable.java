package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CfmlVariable extends CfmlNamedElement {
  @Nullable
  PsiType getPsiType();

  @NotNull
  String getlookUpString();
}
