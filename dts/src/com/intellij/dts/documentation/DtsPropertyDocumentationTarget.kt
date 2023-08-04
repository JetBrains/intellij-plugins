package com.intellij.dts.documentation

import com.intellij.dts.documentation.bindings.DtsZephyrBindingProvider
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.util.DtsHtmlChunk
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.model.Pointer
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.refactoring.suggested.createSmartPointer

class DtsPropertyDocumentationTarget(private val property: DtsProperty) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val propertyPtr = property.createSmartPointer()

        return Pointer {
            propertyPtr.dereference()?.let { DtsPropertyDocumentationTarget(it) }
        }
    }

    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder(property.dtsName).icon(property.getIcon(0)).presentation()
    }

    private fun buildDocumentation(html: DtsDocumentationHtmlBuilder) {
        // write: Property: <<name>>
        html.definition(
            HtmlChunk.fragment(
                DtsHtmlChunk.bundle("documentation.property"),
                HtmlChunk.text(": "),
            ).bold(),
            DtsHtmlChunk.property(property),
        )

        val parent = DtsTreeUtil.parentNode(property) ?: return

        // write: Declared in: <<path>> (<<file>>)
        html.definition(
            HtmlChunk.fragment(
                DtsHtmlChunk.bundle("documentation.declared_in"),
                HtmlChunk.text(": "),
            ).bold(),
            DtsHtmlChunk.path(parent),
            HtmlChunk.text(" (${parent.containingFile.name})").wrapWith(DocumentationMarkup.GRAYED_ELEMENT),
        )

        val provider = DtsZephyrBindingProvider.of(property.project)
        val binding = DtsTreeUtil.search(parent.containingFile, parent, provider::buildBinding)
            ?: provider.buildDefaultBinding()

        binding.propertyDescriptions[property.dtsName]?.let {
            html.content(DtsHtmlChunk.binding(property.project, it))
        }
    }

    override fun computeDocumentation(): DocumentationResult {
        val builder = DtsDocumentationHtmlBuilder()
        buildDocumentation(builder)

        return DocumentationResult.documentation(builder.build())
    }
}