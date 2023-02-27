// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.ConditionalExpression;

public interface ILConditionalExpression extends ILExpression, ConditionalExpression<ILExpression> {

  @NotNull
  ILExpression getCondition();

  @Nullable
  ILExpression getThen();

  @Nullable
  ILExpression getOtherwise();

}
