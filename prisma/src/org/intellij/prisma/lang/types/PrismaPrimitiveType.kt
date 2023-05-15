package org.intellij.prisma.lang.types

import org.intellij.prisma.lang.PrismaConstants

abstract class PrismaPrimitiveType(val name: String) : PrismaType

object PrismaStringType : PrismaPrimitiveType(PrismaConstants.PrimitiveTypes.STRING)
object PrismaBooleanType : PrismaPrimitiveType(PrismaConstants.PrimitiveTypes.BOOLEAN)
object PrismaIntType : PrismaPrimitiveType(PrismaConstants.PrimitiveTypes.INT)
object PrismaFloatType : PrismaPrimitiveType(PrismaConstants.PrimitiveTypes.FLOAT)
object PrismaDateTimeType : PrismaPrimitiveType(PrismaConstants.PrimitiveTypes.DATETIME)
object PrismaJsonType : PrismaPrimitiveType(PrismaConstants.PrimitiveTypes.JSON)
object PrismaBytesType : PrismaPrimitiveType(PrismaConstants.PrimitiveTypes.BYTES)
object PrismaDecimalType : PrismaPrimitiveType(PrismaConstants.PrimitiveTypes.DECIMAL)
object PrismaBigIntType : PrismaPrimitiveType(PrismaConstants.PrimitiveTypes.BIGINT)

class PrismaUnsupportedType(val value: String) : PrismaPrimitiveType(PrismaConstants.PrimitiveTypes.UNSUPPORTED)

object PrismaAnyType : PrismaType