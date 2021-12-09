// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.StubBasedPsiElement;
import com.jetbrains.lang.makefile.stub.MakefileTargetStubElement;
import com.intellij.navigation.ItemPresentation;

public interface MakefileTarget extends MakefileNamedElement, NavigationItem, StubBasedPsiElement<MakefileTargetStubElement> {

  @NotNull
  List<MakefileFunction> getFunctionList();

  @NotNull
  List<MakefileVariableUsage> getVariableUsageList();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String newName);

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  ItemPresentation getPresentation();

  boolean isSpecialTarget();

  boolean isPatternTarget();

  boolean matches(@NotNull String prerequisite);

  @Nullable
  String getDocComment();

}
