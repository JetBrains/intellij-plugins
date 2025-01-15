// This is a generated file. Not intended for manual editing.
package org.intellij.prisma.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import org.intellij.prisma.lang.psi.stubs.PrismaKeyValueStub;

public interface PrismaKeyValue extends StubBasedPsiElement<PrismaKeyValueStub>, PrismaMemberDeclaration {

  @Nullable
  PrismaExpression getExpression();

  @NotNull
  PsiElement getIdentifier();

}
