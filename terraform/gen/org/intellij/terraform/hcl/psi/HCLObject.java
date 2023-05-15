// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.CollectionExpression;
import com.intellij.navigation.ItemPresentation;

public interface HCLObject extends HCLContainer, CollectionExpression<HCLExpression> {

  @NotNull
  List<HCLProperty> getPropertyList();

  @Nullable HCLProperty findProperty(@NotNull String name);

  @Nullable ItemPresentation getPresentation();

  @NotNull List<HCLBlock> getBlockList();

  @NotNull List<HCLExpression> getElements();

}
