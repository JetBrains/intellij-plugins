// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ILTemplateForBlockExpression extends ILExpression {

  @NotNull
  EndFor getEndFor();

  @NotNull
  ForCondition getForCondition();

  @Nullable
  ILTemplateBlockBody getILTemplateBlockBody();

  @NotNull List<ForVariable> getLoopVariables();

}
