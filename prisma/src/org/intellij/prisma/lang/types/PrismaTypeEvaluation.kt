package org.intellij.prisma.lang.types

import com.intellij.openapi.util.text.StringUtil
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.psi.PrismaListType
import org.intellij.prisma.lang.psi.PrismaOptionalType

fun createTypeFromSignature(element: PrismaTypeSignature): PrismaType {
  var type: PrismaType = when (element) {
    is PrismaFieldType -> createTypeFromFieldType(element)
    else -> PrismaAnyType
  }

  val isList = when (element) {
    is PrismaUnsupportedOptionalListType,
    is PrismaListType,
    is PrismaLegacyListType -> true

    else -> false
  }

  val isOptional = when (element) {
    is PrismaOptionalType,
    is PrismaUnsupportedOptionalListType -> true

    else -> false
  }

  if (isList) {
    type = PrismaListType(type)
  }
  if (isOptional) {
    type = PrismaOptionalType(type)
  }
  return type
}

private fun createTypeFromFieldType(element: PrismaFieldType): PrismaType {
  val typeReference = element.typeReference
  val unsupportedType = typeReference.unsupportedType
  if (unsupportedType != null) {
    val value = unsupportedType.stringLiteral?.text
      ?.let { StringUtil.unquoteString(it) }.orEmpty()
    return PrismaUnsupportedType(value)
  }

  val name = typeReference.referenceName ?: return PrismaAnyType

  return when (name) {
    PrimitiveTypes.INT -> PrismaIntType
    PrimitiveTypes.BIGINT -> PrismaBigIntType
    PrimitiveTypes.FLOAT -> PrismaFloatType
    PrimitiveTypes.DECIMAL -> PrismaDecimalType
    PrimitiveTypes.BOOLEAN -> PrismaBooleanType
    PrimitiveTypes.STRING -> PrismaStringType
    PrimitiveTypes.DATETIME -> PrismaDateTimeType
    PrimitiveTypes.JSON -> PrismaJsonType
    PrimitiveTypes.BYTES -> PrismaBytesType
    else -> PrismaReferencedType(name, element)
  }
}