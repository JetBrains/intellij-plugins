// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ILTemplateForBlockExpression extends ILExpression {

  @Nullable
  EndFor getEndFor();

  @NotNull
  ForLoop getForLoop();

  @NotNull List<ForVariable> getLoopVariables();

}
