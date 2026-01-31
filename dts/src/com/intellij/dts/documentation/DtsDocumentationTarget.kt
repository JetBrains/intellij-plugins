package com.intellij.dts.documentation

import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.DtsPropertyType
import com.intellij.dts.lang.DtsPropertyValue
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.getDtsPath
import com.intellij.dts.util.DtsHtmlChunk
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

  private fun buildPropertyValue(propertyType: DtsPropertyType, propertyValue: DtsPropertyValue?): HtmlChunk? {
    if (propertyValue == null || propertyType !in propertyValue.assignableTo) return null

    return when (propertyValue) {
      is DtsPropertyValue.String -> {
        DtsHtmlChunk.string('"' + propertyValue.value + '"')
      }
      is DtsPropertyValue.Int -> {
        val builder = HtmlBuilder()
        builder.append("<").append(DtsHtmlChunk.int(propertyValue.value.toString())).append(">")
        builder.toFragment()

      }
      is DtsPropertyValue.StringList -> {
        val builder = HtmlBuilder()
        builder.appendWithSeparators(HtmlChunk.text(", "), propertyValue.value.map(DtsHtmlChunk::string))
        builder.toFragment()
      }
      is DtsPropertyValue.IntList -> {
        val builder = HtmlBuilder()

        if (propertyType == DtsPropertyType.Bytes) {
          builder.append("[")
          builder.appendWithSeparators(HtmlChunk.text(" "), propertyValue.asByteList().map(DtsHtmlChunk::int))
          builder.append("]")
        }
        else {
          builder.append("<")
          builder.appendWithSeparators(HtmlChunk.text(" "), propertyValue.asIntList().map(DtsHtmlChunk::int))
          builder.append(">")
        }

        builder.toFragment()
      }
    }
  }

  private fun buildPropertyType(html: DtsDocumentationHtmlBuilder, binding: DtsZephyrPropertyBinding) {
    html.definition(DtsHtmlChunk.definitionName("documentation.property_type"))

    val const = buildPropertyValue(binding.type, binding.const)
    if (const != null) {
      val hint = HtmlChunk.text(" (${DtsBundle.message("documentation.property_type.hint_const")})")
      html.appendToDefinition(const, hint.wrapWith(DocumentationMarkup.GRAYED_ELEMENT))
      return
    }

    html.appendToDefinition(HtmlChunk.text(binding.type.typeName))

    if (binding.required) {
      val hint = HtmlChunk.text(" (${DtsBundle.message("documentation.property_type.hint_required")})")
      html.appendToDefinition(hint.wrapWith(DocumentationMarkup.GRAYED_ELEMENT))
    }
  }

  /**
   * Writes: "Type: <<property type>> (<<required | const>>)"
   * And the enum: "In Enum: <<value>>"
   * And the default value: "Default: <<value>>"
   * And the description.
   */
  protected fun buildPropertyBinding(html: DtsDocumentationHtmlBuilder, binding: DtsZephyrPropertyBinding) {
    buildPropertyType(html, binding)

    binding.enum?.let { enum ->
      val builder = HtmlBuilder()
      builder.append("{ ")
      builder.appendWithSeparators(HtmlChunk.text(", "), enum.map { buildPropertyValue(binding.type, it) })
      builder.append(" }")

      html.definition(DtsHtmlChunk.definitionName("documentation.property_enum"), builder.toFragment())
    }

    buildPropertyValue(binding.type, binding.default)?.let { default ->
      html.definition(DtsHtmlChunk.definitionName("documentation.property_default"), default)
    }

    binding.description?.let { description ->
      html.content(DtsHtmlChunk.binding(project, description))
    }
  }

  /**
   * Writes: "[Child of] compatible: <<compatible>> [(on <<bus>> bus)]"
   * And the bus: "Bus controller: <<buses>>"
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

      binding.onBus?.let { bus ->
        val value = DtsBundle.message("documentation.compatible_on_bus", bus)
        val chunk = HtmlChunk.text(" ($value)").wrapWith(DocumentationMarkup.GRAYED_ELEMENT)

        html.appendToDefinition(chunk)
      }
    }

    if (binding.buses.isNotEmpty()) {
      val chunk = HtmlChunk.text(binding.buses.joinToString())
      html.definition(DtsHtmlChunk.definitionName("documentation.bus_controller"), chunk)
    }

    binding.description?.let { description ->
      html.content(DtsHtmlChunk.binding(project, description))
    }
  }
}
