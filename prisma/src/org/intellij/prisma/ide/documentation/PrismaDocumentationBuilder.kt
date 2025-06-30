package org.intellij.prisma.ide.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.ide.schema.builder.*
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.presentation.PrismaPsiRenderer
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.types.isNamedType

private const val PARAM_INDENT = "\n\t\t"
private const val PARAM_SEP = ","
private const val PARAMS_WRAP_LIMIT = 3

class PrismaDocumentationBuilder(private val element: PsiElement) {
  private val psiRenderer = PrismaPsiRenderer()

  @NlsSafe
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
    val file = element.containingFile as? PrismaFile ?: return null
    val datasourceTypes = file.metadata.datasourceTypes
    val params = declaration?.getAvailableParams(datasourceTypes, PrismaSchemaParameterLocation.DEFAULT) ?: emptyList()
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
        val header = if (i == 0) PrismaBundle.message("prisma.doc.section.params") else ""

        section(header) {
          cell { pre(param.label) }
          cellDivider()
          cell(noWrap = false) { append(documentationMarkdownToHtml(param.documentation) ?: "") }
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
    val block = element.getFieldDeclarationBlock() ?: return

    sections {
      val fields = block.fieldDeclarationList
      if (fields.isNotEmpty()) {
        section(PrismaBundle.message("prisma.doc.section.fields")) {
          cell {
            fields.forEach {
              append(psiRenderer.pre(it.identifier).wrapWith(HtmlChunk.p()))
            }
          }
          cellDivider()
          cell {
            fields.forEach {
              append(psiRenderer.span(it.fieldType).wrapWith(HtmlChunk.p()))
            }
          }
        }
      }

      val attributeList = block.blockAttributeList
      if (attributeList.isNotEmpty()) {
        section(PrismaBundle.message("prisma.doc.section.attributes")) {
          DocumentationMarkup.SECTION_CONTENT_CELL.style(NO_WRAP)
            .let { if (fields.isNotEmpty()) it.attr("colspan", 3) else it }
            .let { cell ->
              cell.children(attributeList.map { attr ->
                psiRenderer.pre(attr).wrapWith(HtmlChunk.p())
              })
            }
            .let { append(it) }
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
        cell {
          attributeList.forEach {
            append(psiRenderer.pre(it, noWrap = false).wrapWith(HtmlChunk.p()))
          }
        }
      }
    }
  }

  private fun StringBuilder.cellDivider() {
    append(DocumentationMarkup.SECTION_CONTENT_CELL.style("padding-left: 10px"))
  }

  private fun PrismaPsiRenderer.span(element: PsiElement?, noWrap: Boolean = true): HtmlChunk.Element =
    preformatted(build(element), noWrap, "span")

  private fun PrismaPsiRenderer.pre(element: PsiElement?, noWrap: Boolean = true): HtmlChunk.Element =
    preformatted(build(element), noWrap)

  private fun StringBuilder.pre(@NlsSafe source: String, noWrap: Boolean = true): java.lang.StringBuilder? =
    append(preformatted(source, noWrap))

  private fun preformatted(@NlsSafe source: String, noWrap: Boolean = true, tagName: String = "code"): HtmlChunk.Element {
    val code = HtmlChunk.tag(tagName).let { if (noWrap) it.style(NO_WRAP) else it }
    return HtmlChunk.text(source).wrapWith(code)
  }
}
