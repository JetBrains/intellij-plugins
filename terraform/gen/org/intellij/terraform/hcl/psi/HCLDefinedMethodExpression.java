// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.ProviderDefinedFunction;

public interface HCLDefinedMethodExpression extends HCLExpression, ProviderDefinedFunction<HCLExpression> {

  @NotNull
  List<HCLIdentifier> getIdentifierList();

  @NotNull
  HCLIdentifier getProvider();

  @Nullable
  HCLIdentifier getFunction();

  @NotNull
  HCLParameterList getParameterList();

}
