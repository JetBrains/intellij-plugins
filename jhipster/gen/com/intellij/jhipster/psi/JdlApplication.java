// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;

public interface JdlApplication extends PsiElement {

  @NotNull
  List<JdlConfigBlock> getConfigBlockList();

  @NotNull
  List<JdlConfigurationOption> getConfigurationOptionList();

  @NotNull
  List<JdlUseConfigurationOption> getUseConfigurationOptionList();

  @NotNull String getName();

  @NotNull ItemPresentation getPresentation();

}
