package org.intellij.prisma.ide.documentation

import com.intellij.psi.PsiElement
import org.intellij.prisma.lang.presentation.PrismaPsiRenderer
import org.intellij.prisma.lang.psi.*

private const val UNKNOWN = "<unknown>"

class PrismaDocumentationDefinitionBuilder(private val element: PsiElement) {
  fun buildDefinition(): String? {
    val sb = StringBuilder()

    if (element is PrismaNamedElement) {
      sb.namedElement(element)
    }

    when (element) {
      is PrismaFieldDeclaration -> buildFieldDeclaration(element, sb)
      is PrismaKeyValue -> buildKeyValue(element, sb)
    }

    buildKeyword(sb)

    val text = sb.toString()
    if (text.isEmpty()) {
      return null
    }

    return toHtml(element.project, text)
  }

  private fun buildKeyword(sb: StringBuilder) {
    if (element.isKeyword) {
      sb.append(element.text)
    }
  }

  private fun buildFieldDeclaration(element: PrismaFieldDeclaration, sb: StringBuilder) {
    element.fieldType?.let { fieldType ->
      with(sb) {
        append(": ")
        source(fieldType)
      }
    }
  }

  private fun buildKeyValue(element: PrismaKeyValue, sb: StringBuilder) {
    element.expression?.let {
      with(sb) {
        append(" = ")
        source(it)
      }
    }
  }

  private fun StringBuilder.source(element: PsiElement?) = append(PrismaPsiRenderer().build(element))

  private fun StringBuilder.namedElement(element: PrismaNamedElement) {
    val name = element.name ?: UNKNOWN

    keyword(element)
    qualifier(element)
    append(name)
  }

  private fun StringBuilder.qualifier(element: PrismaNamedElement) {
    if (element is PrismaMemberDeclaration) {
      element.containingDeclaration?.name?.let {
        append(it)
        append(".")
      }
    }
  }

  private fun StringBuilder.keyword(element: PsiElement) {
    element.keyword?.let {
      append(it)
      append(" ")
    }
  }

  private val PsiElement.keyword: String?
    get() {
      return when (this) {
        is PrismaModelDeclaration -> "model"
        is PrismaTypeDeclaration -> "type"
        is PrismaViewDeclaration -> "view"
        is PrismaDatasourceDeclaration -> "datasource"
        is PrismaGeneratorDeclaration -> "generator"
        is PrismaTypeAlias -> "type"
        is PrismaEnumDeclaration -> "enum"
        else -> null
      }
    }
}