// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.SelectExpression;
import com.intellij.psi.PsiReference;

public interface HCLSelectExpression extends HCLExpression, SelectExpression<HCLExpression> {

  @NotNull
  HCLExpression getFrom();

  @Nullable
  HCLExpression getField();

  @Nullable PsiReference getReference();

  PsiReference @NotNull [] getReferences();

}
