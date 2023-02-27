// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.navigation.ItemPresentation;

public interface HCLProperty extends HCLElement, PsiNameIdentifierOwner {

  @NotNull String getName();

  @NotNull HCLExpression getNameElement();

  @Nullable HCLExpression getValue();

  @Nullable ItemPresentation getPresentation();

}
