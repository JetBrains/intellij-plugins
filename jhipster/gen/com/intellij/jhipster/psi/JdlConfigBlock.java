// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;

public interface JdlConfigBlock extends PsiElement {

  @NotNull
  JdlConfigKeyword getConfigKeyword();

  @NotNull
  List<JdlOptionNameValue> getOptionNameValueList();

  @NotNull ItemPresentation getPresentation();

}
