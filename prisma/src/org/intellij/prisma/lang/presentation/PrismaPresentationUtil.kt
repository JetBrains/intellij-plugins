package org.intellij.prisma.lang.presentation

import com.intellij.navigation.ItemPresentation
import org.intellij.prisma.PrismaIcons
import org.intellij.prisma.ide.schema.*
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.types.typeText
import javax.swing.Icon

fun getPresentation(element: PrismaElement): ItemPresentation = object : ItemPresentation {
  override fun getPresentableText(): String? {
    if (element is PrismaNamedElement) {
      return buildString {
        append(element.name)

        if (element is PrismaFieldDeclaration) {
          val typeText = element.type.typeText
          if (typeText.isNotBlank()) {
            append(": ")
            append(typeText)
          }
        }
      }
    }

    return null
  }

  override fun getLocationString(): String = element.containingFile.name

  override fun getIcon(unused: Boolean): Icon? = element.getIcon(0)
}

val PrismaNamedElement.icon: Icon?
  get() = when (this) {
    is PrismaModelTypeDeclaration -> PrismaIcons.TYPE
    is PrismaKeyValueDeclaration -> PrismaIcons.KEY_VALUE
    is PrismaEnumDeclaration -> PrismaIcons.ENUM
    is PrismaTypeAlias -> PrismaIcons.ALIAS
    is PrismaKeyValue -> PrismaIcons.KEY_VALUE
    is PrismaMemberDeclaration -> PrismaIcons.FIELD
    else -> null
  }

val PrismaSchemaElement.icon: Icon?
  get() = when (this) {
    is PrismaSchemaParameter -> PrismaIcons.PARAMETER

    is PrismaSchemaDeclaration -> when (kind) {
      PrismaSchemaKind.PRIMITIVE_TYPE -> PrismaIcons.TYPE
      PrismaSchemaKind.GENERATOR_FIELD, PrismaSchemaKind.DATASOURCE_FIELD -> PrismaIcons.KEY_VALUE
      PrismaSchemaKind.BLOCK_ATTRIBUTE, PrismaSchemaKind.FIELD_ATTRIBUTE -> PrismaIcons.ATTRIBUTE
      PrismaSchemaKind.FUNCTION -> PrismaIcons.FUNCTION
      else -> null
    }

    is PrismaSchemaVariant -> PrismaIcons.FIELD
  }

