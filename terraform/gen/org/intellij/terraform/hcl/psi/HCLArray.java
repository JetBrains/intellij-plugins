// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.CollectionExpression;
import com.intellij.navigation.ItemPresentation;

public interface HCLArray extends HCLContainer, CollectionExpression<HCLExpression> {

  @Nullable ItemPresentation getPresentation();

  @NotNull
  List<HCLExpression> getElements();

}
