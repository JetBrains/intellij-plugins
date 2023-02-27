// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HCLForObjectExpression extends HCLForExpression {

  @NotNull HCLExpression getKey();

  @NotNull HCLExpression getValue();

  boolean isGrouping();

}
