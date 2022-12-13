package org.intellij.prisma.ide.schema.definitions

import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import org.intellij.prisma.ide.schema.PrismaSchemaParameter
import org.intellij.prisma.ide.schema.types.PrismaIndexAlgorithm
import org.intellij.prisma.ide.schema.types.PrismaReferentialAction
import org.intellij.prisma.ide.schema.types.PrismaSortOrder
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes
import org.intellij.prisma.lang.PrismaConstants.Types
import org.intellij.prisma.lang.types.parseTypeName

fun PrismaSchemaParameter.Builder.variantsForType(type: String) {
  when (parseTypeName(type)) {
    PrimitiveTypes.BOOLEAN -> booleanTypeValues()
    Types.SORT_ORDER -> sortOrderTypeValues()
    Types.INDEX_TYPE -> indexingAlgorithmTypeValues()
    Types.REFERENTIAL_ACTION -> referentialActionTypeValues()
  }
}

fun PrismaSchemaParameter.Builder.booleanTypeValues(elementPattern: ElementPattern<out PsiElement>? = null) {
  variant {
    label = "true"
    type = PrimitiveTypes.BOOLEAN
    pattern = elementPattern
  }
  variant {
    label = "false"
    type = PrimitiveTypes.BOOLEAN
    pattern = elementPattern
  }
}

private fun PrismaSchemaParameter.Builder.sortOrderTypeValues() {
  PrismaSortOrder.values().forEach {
    variant {
      label = it.name
      type = Types.SORT_ORDER
    }
  }
}

private fun PrismaSchemaParameter.Builder.indexingAlgorithmTypeValues() {
  PrismaIndexAlgorithm.values().forEach {
    variant {
      label = it.name
      documentation = it.documentation
      type = Types.INDEX_TYPE
    }
  }
}

private fun PrismaSchemaParameter.Builder.referentialActionTypeValues() {
  PrismaReferentialAction.values().forEach {
    variant {
      label = it.name
      documentation = it.documentation
      type = Types.REFERENTIAL_ACTION
      datasources = it.datasources
    }
  }
}