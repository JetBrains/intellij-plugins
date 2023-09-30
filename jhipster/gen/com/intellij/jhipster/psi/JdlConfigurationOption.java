// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;

public interface JdlConfigurationOption extends PsiElement {

  @NotNull
  JdlConfigurationOptionName getConfigurationOptionName();

  @Nullable
  JdlEntitiesList getEntitiesList();

  @Nullable
  JdlExceptEntities getExceptEntities();

  @Nullable
  JdlWildcardLiteral getWildcardLiteral();

  @Nullable
  JdlWithOptionValue getWithOptionValue();

  @NotNull String getName();

  @NotNull JdlConfigurationOptionName getNameElement();

  @NotNull ItemPresentation getPresentation();

}
