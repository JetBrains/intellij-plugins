// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.LiteralExpression;

public interface ILLiteralExpression extends ILExpression, LiteralExpression {

  @Nullable
  PsiElement getDoubleQuotedString();

  @Nullable
  PsiElement getNumber();

  @NotNull String getUnquotedText();

}
