// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.UnaryExpression;
import com.intellij.psi.tree.IElementType;

public interface ILUnaryExpression extends ILExpression, UnaryExpression<ILExpression> {

  @Nullable
  ILExpression getOperand();

  @NotNull IElementType getOperationSign();

}
