// This is a generated file. Not intended for manual editing.
package org.intellij.prisma.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import org.intellij.prisma.lang.psi.stubs.PrismaFieldDeclarationStub;

public interface PrismaFieldDeclaration extends StubBasedPsiElement<PrismaFieldDeclarationStub>, PrismaMemberDeclaration, PrismaTypeOwner, PrismaFieldAttributeOwner {

  @NotNull
  List<PrismaFieldAttribute> getFieldAttributeList();

  @Nullable
  PrismaFieldType getFieldType();

  @NotNull
  PsiElement getIdentifier();

  @Nullable String getNativeType();

}
