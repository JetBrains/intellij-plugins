// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.MethodCallExpression;

public interface HCLMethodCallExpression extends HCLExpression, MethodCallExpression<HCLExpression> {

  @NotNull
  HCLIdentifier getCallee();

  @NotNull
  HCLParameterList getParameterList();

  //WARNING: getQualifier(...) is skipped
  //matching getQualifier(HCLMethodCallExpression, ...)
  //methods are not found in HCLPsiImplUtilJ

  @NotNull HCLIdentifier getMethod();

}
