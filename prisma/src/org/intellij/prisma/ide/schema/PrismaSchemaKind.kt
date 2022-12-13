package org.intellij.prisma.ide.schema

enum class PrismaSchemaKind {
  KEYWORD,
  PRIMITIVE_TYPE,
  DATASOURCE_FIELD,
  GENERATOR_FIELD,
  BLOCK_ATTRIBUTE,
  FIELD_ATTRIBUTE,
  FUNCTION,
}