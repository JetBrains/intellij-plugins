package org.intellij.prisma.ide.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.ide.schema.*
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.presentation.PrismaPsiRenderer
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.types.isNamedType

private const val PARAM_INDENT = "\n\t\t"
private const val PARAM_SEP = ","
private const val PARAMS_WRAP_LIMIT = 3

class PrismaDocumentationBuilder(private val element: PsiElement) {
  private val psiRenderer = PrismaPsiRenderer()

  fun buildDocumentation(): String? {
    val schemaDoc = buildDocumentationForSchemaElement(element)
    if (schemaDoc != null) {
      return schemaDoc
    }

    val definitionBuilder = PrismaDocumentationDefinitionBuilder(element)
    val def = definitionBuilder.buildDefinition() ?: return null

    return buildString {
      definition { append(def) }
      documentationComment(element)
      additionalSections()
    }
  }

  private fun buildDocumentationForSchemaElement(element: PsiElement): String? {
    if (skipForDeprecatedElement(element)) {
      return null
    }

    val schema = PrismaSchemaProvider.getEvaluatedSchema(PrismaSchemaEvaluationContext.forElement(element))
    val schemaElement = schema.match(element) ?: return null
    val declaration = schemaElement as? PrismaSchemaDeclaration
    val file = element.containingFile as? PrismaFile
    val params = declaration?.getAvailableParams(file?.datasourceType, false) ?: emptyList()
    val definition = declaration?.signature ?: buildDefinitionFromSchema(schemaElement, params)

    return buildString {
      definition {
        val renderAsPlainText = schemaElement is PrismaSchemaVariant && isNamedType(
          schemaElement.type,
          PrismaConstants.PrimitiveTypes.STRING
        )

        if (renderAsPlainText) {
          append(definition)
        }
        else {
          append(toHtml(element.project, definition))
        }
      }

      documentationMarkdownToHtml(schemaElement.documentation)?.let {
        content {
          append(it)
        }
      }

      paramsSection(params)
    }
  }

  private fun skipForDeprecatedElement(element: PsiElement): Boolean {
    return element.elementType == PrismaElementTypes.TYPE && element.parent is PrismaTypeAlias
  }

  private fun buildDefinitionFromSchema(
    schemaElement: PrismaSchemaElement,
    params: List<PrismaSchemaParameter>,
  ): String {
    return buildString {
      append(schemaElement.label)

      if (schemaElement is PrismaSchemaDeclaration &&
          (params.isNotEmpty() || schemaElement.kind == PrismaSchemaKind.FUNCTION)
      ) {
        val indent = if (params.size > PARAMS_WRAP_LIMIT) {
          PARAM_INDENT
        }
        else {
          ""
        }
        val separator = PARAM_SEP + indent.ifEmpty { " " }

        params.joinTo(this, separator = separator, prefix = "($indent", postfix = ")") {
          val type = if (it.type != null) {
            ": ${it.type}"
          }
          else {
            ""
          }

          "${it.label}${type}"
        }
      }
      else if (schemaElement is PrismaSchemaParameter) {
        schemaElement.type?.let {
          append(": $it")
        }
      }
    }
  }

  private fun StringBuilder.paramsSection(params: List<PrismaSchemaParameter>) {
    if (params.isEmpty()) return

    sections {
      for ((i, param) in params.withIndex()) {
        val header = if (i == 0) {
          PrismaBundle.message("prisma.doc.section.params")
        }
        else {
          ""
        }

        section(header) {
          pre(param.label)
          cellWithLeftPadding()
          append(documentationMarkdownToHtml(param.documentation) ?: "")
        }
      }
    }
  }

  private fun StringBuilder.additionalSections() {
    when (element) {
      is PrismaFieldDeclaration -> fieldAttributesSection(element)
      is PrismaTableEntityDeclaration -> tableEntityMembers(element)
    }
  }

  private fun StringBuilder.tableEntityMembers(element: PrismaTableEntityDeclaration) {
    sections {
      val block = element.getFieldDeclarationBlock() ?: return@sections

      val fields = block.fieldDeclarationList
      if (fields.isNotEmpty()) {
        section(PrismaBundle.message("prisma.doc.section.fields")) {
          fields.joinTo(this, separator = "<p>") {
            psiRenderer.pre(it.identifier)
          }
          cellWithLeftPadding()
          fields.joinTo(this, separator = "<p>") {
            psiRenderer.pre(it.fieldType)
          }
        }
      }

      val attributeList = block.blockAttributeList
      if (attributeList.isNotEmpty()) {
        section(
          PrismaBundle.message("prisma.doc.section.attributes"),
          DocumentationMarkup.SECTION_CONTENT_CELL.attr("colspan", 2).style("white-space: nowrap")
        ) {
          attributeList.joinTo(this, separator = "<p>") { psiRenderer.pre(it) }
        }
      }
    }
  }

  private fun StringBuilder.fieldAttributesSection(element: PrismaFieldDeclaration) {
    val attributeList = element.fieldAttributeList
    if (attributeList.isEmpty()) {
      return
    }

    sections {
      section(PrismaBundle.message("prisma.doc.section.attributes")) {
        attributeList.joinTo(this, separator = "<p>") { psiRenderer.pre(it) }
      }
    }
  }

  private fun StringBuilder.cellWithLeftPadding() {
    append(DocumentationMarkup.SECTION_CONTENT_CELL.style("padding-left: 15px"))
  }

  private fun PrismaPsiRenderer.pre(element: PsiElement?) =
    HtmlChunk.text(build(element)).code().toString()

  private fun StringBuilder.pre(@NlsSafe source: String) =
    append(HtmlChunk.text(source).code().toString())
}
