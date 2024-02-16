// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.MethodCallExpression;

public interface ILMethodCallExpression extends ILExpression, MethodCallExpression<ILExpression> {

  @NotNull
  ILExpression getCallee();

  @NotNull
  ILParameterList getParameterList();

  ILExpression getQualifier();

  @Nullable ILVariable getMethod();

}
