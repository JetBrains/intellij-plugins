package org.intellij.prisma.ide.schema.types

import org.intellij.prisma.lang.types.PrismaType

class PrismaNativeTypeConstructor(
  val name: String,
  val numberOfArgs: Int,
  val numberOfOptionalArgs: Int,
  val types: List<PrismaType>,
) {
  companion object {
    fun withoutArgs(name: String, types: List<PrismaType>): PrismaNativeTypeConstructor =
      PrismaNativeTypeConstructor(name, 0, 0, types)

    fun withArgs(name: String, numberOfArgs: Int, types: List<PrismaType>): PrismaNativeTypeConstructor =
      PrismaNativeTypeConstructor(name, numberOfArgs, 0, types)

    fun withOptionalArgs(
      name: String,
      numberOfOptionalArgs: Int,
      types: List<PrismaType>
    ): PrismaNativeTypeConstructor =
      PrismaNativeTypeConstructor(name, 0, numberOfOptionalArgs, types)
  }
}