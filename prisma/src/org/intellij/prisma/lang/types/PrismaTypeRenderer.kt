package org.intellij.prisma.lang.types

import com.intellij.openapi.diagnostic.logger
import org.intellij.prisma.lang.PrismaConstants

private val LOG = logger<PrismaTypeRenderer>()

class PrismaTypeRenderer {
  fun render(type: PrismaType): String {
    return buildString {
      build(this, type)
    }
  }

  private fun build(sb: StringBuilder, type: PrismaType) {
    when (type) {
      is PrismaUnsupportedType -> {
        sb.append(PrismaConstants.PrimitiveTypes.UNSUPPORTED)
        sb.append("(\"")
        sb.append(type.value)
        sb.append("\")")
      }

      is PrismaPrimitiveType -> sb.append(type.name)
      is PrismaCompositeType -> sb.append(type.name)
      is PrismaReferencedType -> sb.append(type.name)
      is PrismaListType -> {
        build(sb, type.innerType)
        sb.append("[]")
      }

      is PrismaOptionalType -> {
        build(sb, type.innerType)
        sb.append("?")
      }

      is PrismaAnyType -> {}

      else -> LOG.error("unknown type: ${type.javaClass.name}")
    }
  }
}