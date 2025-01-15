// This is a generated file. Not intended for manual editing.
package org.intellij.prisma.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import org.intellij.prisma.lang.psi.stubs.PrismaGeneratorDeclarationStub;

public interface PrismaGeneratorDeclaration extends StubBasedPsiElement<PrismaGeneratorDeclarationStub>, PrismaDeclaration, PrismaKeyValueDeclaration {

  @Nullable
  PrismaKeyValueBlock getKeyValueBlock();

  @Nullable
  PsiElement getIdentifier();

}
