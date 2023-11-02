package com.intellij.dts.documentation

import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.getDtsPath
import com.intellij.dts.util.DtsHtmlChunk
import com.intellij.dts.zephyr.binding.DtsZephyrBinding
import com.intellij.dts.zephyr.binding.DtsZephyrPropertyBinding
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.project.Project
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

    /**
     * Writes: "Type: <<property type>> (<<required>>)"
     * And the description.
     */
    protected fun buildPropertyBinding(html: DtsDocumentationHtmlBuilder, binding: DtsZephyrPropertyBinding) {
        html.definition(DtsHtmlChunk.definitionName("documentation.property_type"))
        html.appendToDefinition(HtmlChunk.text(binding.type.typeName))

        if (binding.required) {
            html.appendToDefinition(
                HtmlChunk.text(" (${DtsBundle.message("documentation.required")})").wrapWith(DocumentationMarkup.GRAYED_ELEMENT),
            )
        }

        binding.description?.let {
            html.content(DtsHtmlChunk.binding(project, it))
        }
    }

    /**
     * Writes: "[Child of] compatible: <<compatible>>"
     * And the description.
     */
    protected fun buildNodeBinding(html: DtsDocumentationHtmlBuilder, binding: DtsZephyrBinding) {
        binding.compatible?.let { compatible ->
            if (binding.isChild) {
                html.definition(DtsHtmlChunk.definitionName("documentation.compatible_child"))
            } else {
                html.definition(DtsHtmlChunk.definitionName("documentation.compatible"))
            }
            html.appendToDefinition(DtsHtmlChunk.string(compatible))
        }

        binding.description?.let { description ->
            html.content(DtsHtmlChunk.binding(project, description))
        }
    }
}