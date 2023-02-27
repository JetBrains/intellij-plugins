// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.ConditionalExpression;

public interface HCLConditionalExpression extends HCLExpression, ConditionalExpression<HCLExpression> {

  @NotNull
  List<HCLExpression> getExpressionList();

  @NotNull
  HCLExpression getCondition();

  @Nullable
  HCLExpression getThen();

  @Nullable
  HCLExpression getOtherwise();

}
