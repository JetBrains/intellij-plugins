// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.BinaryExpression;
import com.intellij.psi.tree.IElementType;

public interface ILBinaryExpression extends ILExpression, BinaryExpression<ILExpression> {

  @NotNull
  ILExpression getLeftOperand();

  @Nullable
  ILExpression getRightOperand();

  @NotNull IElementType getOperationSign();

}
