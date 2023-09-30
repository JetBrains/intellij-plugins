// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;

public interface JdlEnumValue extends PsiElement {

  @NotNull
  JdlEnumKey getEnumKey();

  @Nullable
  JdlExplicitEnumMapping getExplicitEnumMapping();

  @NotNull String getName();

  @NotNull JdlEnumKey getNameElement();

  @NotNull ItemPresentation getPresentation();

}
