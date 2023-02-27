// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.navigation.ItemPresentation;

public interface HCLBlock extends HCLElement, PsiNameIdentifierOwner {

  @NotNull String getName();

  @NotNull String getFullName();

  HCLElement @NotNull [] getNameElements();

  @Nullable HCLObject getObject();

  @Nullable ItemPresentation getPresentation();

}
