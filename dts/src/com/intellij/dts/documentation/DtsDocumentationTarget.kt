package com.intellij.dts.documentation

import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.DtsPropertyType
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.getDtsPath
import com.intellij.dts.util.DtsHtmlChunk
import com.intellij.dts.zephyr.binding.DtsPropertyValue
import com.intellij.dts.zephyr.binding.DtsZephyrBinding
import com.intellij.dts.zephyr.binding.DtsZephyrPropertyBinding
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget

abstract class DtsDocumentationTarget(protected val project: Project) : DocumentationTarget {
  protected abstract fun buildDocumentation(html: DtsDocumentationHtmlBuilder)

  final override fun computeDocumentation(): DocumentationResult {
    val builder = DtsDocumentationHtmlBuilder()
    buildDocumentation(builder)

    return DocumentationResult.documentation(builder.build())
  }

  /**
   * Writes: "Property: <<name>>"
   */
  protected fun buildPropertyName(html: DtsDocumentationHtmlBuilder, name: String) {
    html.definition(DtsHtmlChunk.definitionName("documentation.property"))
    html.appendToDefinition(DtsHtmlChunk.propertyName(name))
  }

  /**
   * Writes: "Node <<name>>"
   */
  protected fun buildNodeName(html: DtsDocumentationHtmlBuilder, name: String) {
    html.definition(DtsHtmlChunk.definitionName("documentation.node"))
    html.appendToDefinition(DtsHtmlChunk.nodeName(name))
  }

  /**
   * Writes: "Node <<name>>"
   */
  protected fun buildNodeName(html: DtsDocumentationHtmlBuilder, node: DtsNode) {
    html.definition(DtsHtmlChunk.definitionName("documentation.node"))
    html.appendToDefinition(DtsHtmlChunk.node(node))
  }

  /**
   * Writes: "Declared in: <<path>> (<<file>>)"
   */
  protected fun buildDeclaredIn(html: DtsDocumentationHtmlBuilder, parent: DtsNode) {
    val path = parent.getDtsPath() ?: return

    html.definition(DtsHtmlChunk.definitionName("documentation.declared_in"))
    html.appendToDefinition(
      DtsHtmlChunk.path(path),
      HtmlChunk.text(" (${parent.containingFile.name})").wrapWith(DocumentationMarkup.GRAYED_ELEMENT),
    )
  }

  private fun buildPropertyDefault(html: DtsDocumentationHtmlBuilder, type: DtsPropertyType, default: DtsPropertyValue) {
    if (type !in default.assignableTo) return

    val value = when (default) {
      is DtsPropertyValue.String -> {
        DtsHtmlChunk.string('"' + default.value + '"')
      }
      is DtsPropertyValue.Int -> {
        val builder = HtmlBuilder()
        builder.append("<").append(DtsHtmlChunk.int(default.value.toString())).append(">")
        builder.toFragment()

      }
      is DtsPropertyValue.StringList -> {
        val builder = HtmlBuilder()
        builder.appendWithSeparators(HtmlChunk.text(", "), default.value.map(DtsHtmlChunk::string))
        builder.toFragment()
      }
      is DtsPropertyValue.IntList -> {
        val builder = HtmlBuilder()

        if (type == DtsPropertyType.Bytes) {
          builder.append("[")
          builder.appendWithSeparators(HtmlChunk.text(" "), default.asByteList().map(DtsHtmlChunk::int))
          builder.append("]")
        } else {
          builder.append("<")
          builder.appendWithSeparators(HtmlChunk.text(" "), default.asIntList().map(DtsHtmlChunk::int))
          builder.append(">")
        }

        builder.toFragment()
      }
    } ?: return

    html.definition(DtsHtmlChunk.definitionName("documentation.property_default"), value)
  }

  /**
   * Writes: "Type: <<property type>> (<<required>>)"
   * And the description.
   */
  protected fun buildPropertyBinding(html: DtsDocumentationHtmlBuilder, binding: DtsZephyrPropertyBinding) {
    html.definition(DtsHtmlChunk.definitionName("documentation.property_type"))
    html.appendToDefinition(HtmlChunk.text(binding.type.typeName))

    if (binding.required) {
      html.appendToDefinition(HtmlChunk.text(" (${DtsBundle.message("documentation.required")})").wrapWith(DocumentationMarkup.GRAYED_ELEMENT))
    }

    binding.default?.let { default -> buildPropertyDefault(html, binding.type, default) }
    binding.description?.let { description -> html.content(DtsHtmlChunk.binding(project, description)) }
  }

  /**
   * Writes: "[Child of] compatible: <<compatible>>"
   * And the description.
   */
  protected fun buildNodeBinding(html: DtsDocumentationHtmlBuilder, binding: DtsZephyrBinding) {
    binding.compatible?.let { compatible ->
      if (binding.isChild) {
        html.definition(DtsHtmlChunk.definitionName("documentation.compatible_child"))
      }
      else {
        html.definition(DtsHtmlChunk.definitionName("documentation.compatible"))
      }
      html.appendToDefinition(DtsHtmlChunk.string(compatible))
    }

    binding.description?.let { description ->
      html.content(DtsHtmlChunk.binding(project, description))
    }
  }
}
