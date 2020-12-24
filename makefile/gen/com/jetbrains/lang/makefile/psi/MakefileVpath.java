// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface MakefileVpath extends PsiElement {

  @NotNull
  List<MakefileDirectory> getDirectoryList();

  @Nullable
  MakefilePattern getPattern();

}
