package com.intellij.dts.documentation

import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.util.DtsHtmlChunk
import com.intellij.dts.zephyr.DtsZephyrBinding
import com.intellij.dts.zephyr.DtsZephyrPropertyBinding
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import org.jetbrains.annotations.PropertyKey

abstract class DtsDocumentationTarget(protected val project: Project) : DocumentationTarget {
    protected abstract fun buildDocumentation(html: DtsDocumentationHtmlBuilder)

    final override fun computeDocumentation(): DocumentationResult {
        val builder = DtsDocumentationHtmlBuilder()
        buildDocumentation(builder)

        return DocumentationResult.documentation(builder.build())
    }

    /**
     * Writes: "<<name>>: <<value>>"
     */
    protected fun buildDefinition(html: DtsDocumentationHtmlBuilder, name: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String, value: HtmlChunk) {
        html.definition(
            HtmlChunk.fragment(
                DtsHtmlChunk.bundle(name),
                HtmlChunk.text(": "),
            ).bold(),
            value,
        )
    }

    /**
     * Writes: "Property: <<name>>"
     */
    protected fun buildPropertyName(html: DtsDocumentationHtmlBuilder, name: String) =
        buildDefinition(html, "documentation.property", DtsHtmlChunk.propertyName(name))

    /**
     * Writes: "Node <<name>>"
     */
    protected fun buildNodeName(html: DtsDocumentationHtmlBuilder, name: String) =
        buildDefinition(html, "documentation.node", DtsHtmlChunk.nodeName(name))

    /**
     * Writes: "Node <<name>>"
     */
    protected fun buildNodeName(html: DtsDocumentationHtmlBuilder, node: DtsNode) =
        buildDefinition(html, "documentation.node", DtsHtmlChunk.node(node))

    /**
     * Writes: "Declared in: <<path>> (<<file>>)"
     */
    protected fun buildDeclaredIn(html: DtsDocumentationHtmlBuilder, parent: DtsNode) {
        html.definition(
            HtmlChunk.fragment(
                DtsHtmlChunk.bundle("documentation.declared_in"),
                HtmlChunk.text(": "),
            ).bold(),
            DtsHtmlChunk.path(parent),
            HtmlChunk.text(" (${parent.containingFile.name})").wrapWith(DocumentationMarkup.GRAYED_ELEMENT),
        )
    }

    /**
     * Writes: "Type: <<property type>>"
     * And the description.
     */
    protected fun buildPropertyBinding(html: DtsDocumentationHtmlBuilder, binding: DtsZephyrPropertyBinding) {
        // write: Type: <<property type>>
        html.definition(
            HtmlChunk.fragment(
                DtsHtmlChunk.bundle("documentation.property_type"),
                HtmlChunk.text(": "),
            ).bold(),
            HtmlChunk.text(binding.type.typeName),
        )

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
            html.definition(
                HtmlChunk.fragment(
                    if (binding.isChild) {
                        DtsHtmlChunk.bundle("documentation.compatible_child")
                    } else {
                        DtsHtmlChunk.bundle("documentation.compatible")
                    },
                    HtmlChunk.text(": "),
                ).bold(),
                DtsHtmlChunk.string(compatible),
            )
        }

        binding.description?.let { description ->
            html.content(DtsHtmlChunk.binding(project, description))
        }
    }
}