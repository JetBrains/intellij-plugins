package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 */
public interface CfmlTypedElement {
  @Nullable
  PsiType getPsiType();
}
