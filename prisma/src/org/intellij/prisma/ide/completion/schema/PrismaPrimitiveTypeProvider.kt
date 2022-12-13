package org.intellij.prisma.ide.completion.schema

import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.lang.psi.PrismaPsiPatterns

object PrismaPrimitiveTypeProvider : PrismaSchemaCompletionProvider() {
  override val kind: PrismaSchemaKind = PrismaSchemaKind.PRIMITIVE_TYPE

  override val pattern = PrismaPsiPatterns.typeReference
}

