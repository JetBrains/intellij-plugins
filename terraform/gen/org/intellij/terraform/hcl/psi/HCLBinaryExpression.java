// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.BinaryExpression;
import com.intellij.psi.tree.IElementType;

public interface HCLBinaryExpression extends HCLExpression, BinaryExpression<HCLExpression> {

  @NotNull
  HCLExpression getLeftOperand();

  @Nullable
  HCLExpression getRightOperand();

  @NotNull IElementType getOperationSign();

}
