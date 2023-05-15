// This is a generated file. Not intended for manual editing.
package org.intellij.prisma.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PrismaEnumDeclarationBlock extends PrismaBlock {

  @NotNull
  List<PrismaBlockAttribute> getBlockAttributeList();

  @NotNull
  List<PrismaEnumValueDeclaration> getEnumValueDeclarationList();

}
