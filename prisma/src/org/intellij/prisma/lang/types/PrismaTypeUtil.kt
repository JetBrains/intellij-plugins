package org.intellij.prisma.lang.types

fun PrismaType.unwrapType(): PrismaType {
  if (this is PrismaDecoratedType) {
    return this.unwrap()
  }
  return this
}

val PrismaType.name: String?
  get() = when (val underlyingType = unwrapType()) {
    is PrismaPrimitiveType -> underlyingType.name
    is PrismaReferencedType -> underlyingType.name
    else -> null
  }


fun PrismaType?.anyTypeMatching(predicate: (PrismaType) -> Boolean): Boolean {
  if (this == null) return false

  var current = this
  while (current != null) {
    if (predicate(current)) {
      return true
    }
    if (current is PrismaDecoratedType) {
      current = current.innerType
    }
    else {
      return false
    }
  }
  return false
}

fun PrismaType?.isList(): Boolean {
  return anyTypeMatching { it is PrismaListType }
}

fun PrismaType?.isOptional(): Boolean {
  return anyTypeMatching { it is PrismaOptionalType }
}

fun parseTypeName(type: String?): String? {
  return type?.removeSuffix("?")?.removeSuffix("[]")
}

fun isNamedType(type: String?, expected: String): Boolean {
  return parseTypeName(type) == expected
}

fun isListType(type: String?): Boolean {
  return type?.contains("[]") ?: false
}

fun isOptionalType(type: String?): Boolean {
  return type?.contains("?") ?: false
}

val PrismaType.typeText: String
  get() = PrismaTypeRenderer().render(this)