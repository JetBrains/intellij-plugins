// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.ParameterList;

public interface HCLParameterList extends PsiElement, ParameterList<HCLExpression> {

  @NotNull
  List<HCLExpression> getElements();

}
