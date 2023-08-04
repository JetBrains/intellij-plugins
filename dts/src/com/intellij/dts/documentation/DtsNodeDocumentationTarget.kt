package com.intellij.dts.documentation

import com.intellij.dts.documentation.bindings.DtsZephyrBindingProvider
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.getDtsPresentableText
import com.intellij.dts.util.DtsHtmlChunk
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.model.Pointer
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.refactoring.suggested.createSmartPointer

class DtsNodeDocumentationTarget(private val node: DtsNode) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val propertyPtr = node.createSmartPointer()

        return Pointer {
            propertyPtr.dereference()?.let { DtsNodeDocumentationTarget(it) }
        }
    }

    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder(node.getDtsPresentableText()).icon(node.getIcon(0)).presentation()
    }

    private fun buildDocumentation(html: DtsDocumentationHtmlBuilder) {
        // write: Node: <<name>>
        html.definition(
            HtmlChunk.fragment(
                DtsHtmlChunk.bundle("documentation.node"),
                HtmlChunk.text(": "),
            ).bold(),
            DtsHtmlChunk.node(node),
        )

        // write: Declared in: <<path>> (<<file>>)
        DtsTreeUtil.parentNode(node)?.let { parent ->
            html.definition(
                HtmlChunk.fragment(
                    DtsHtmlChunk.bundle("documentation.declared_in"),
                    HtmlChunk.text(": "),
                ).bold(),
                DtsHtmlChunk.path(parent),
                HtmlChunk.text(" (${parent.containingFile.name})").wrapWith(DocumentationMarkup.GRAYED_ELEMENT),
            )
        }

        val provider = DtsZephyrBindingProvider.of(node.project)
        val binding = DtsTreeUtil.search(node.containingFile, node, provider::buildBinding) ?: return

        // write: [Child of] compatible: <<compatible>>
        html.definition(
            HtmlChunk.fragment(
                if (binding.isChild) {
                    DtsHtmlChunk.bundle("documentation.compatible_child")
                } else {
                    DtsHtmlChunk.bundle("documentation.compatible")
                },
                HtmlChunk.text(": "),
            ).bold(),
            DtsHtmlChunk.string(binding.compatible),
        )

        binding.description?.let {
            html.content(DtsHtmlChunk.binding(node.project, it))
        }
    }

    override fun computeDocumentation(): DocumentationResult {
        val builder = DtsDocumentationHtmlBuilder()
        buildDocumentation(builder)

        return DocumentationResult.documentation(builder.build())
    }
}