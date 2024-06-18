// This is a generated file. Not intended for manual editing.
package org.intellij.prisma.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import org.intellij.prisma.lang.psi.stubs.PrismaNamedStub;

public interface PrismaKeyValue extends StubBasedPsiElement<PrismaNamedStub<PrismaKeyValue>>, PrismaMemberDeclaration {

  @Nullable
  PrismaExpression getExpression();

  @NotNull
  PsiElement getIdentifier();

}
