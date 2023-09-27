package com.intellij.dts.documentation

import com.intellij.dts.DtsIcons
import com.intellij.dts.util.DtsHtmlChunk
import com.intellij.dts.zephyr.DtsZephyrPropertyBinding
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation

class DtsPropertyBindingDocumentationTarget(private val project: Project, private val binding: DtsZephyrPropertyBinding) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> = Pointer { this }

    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder(binding.name).icon(DtsIcons.Property).presentation()
    }

    private fun buildDocumentation(html: DtsDocumentationHtmlBuilder) {
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

    override fun computeDocumentation(): DocumentationResult {
       val builder = DtsDocumentationHtmlBuilder()
       buildDocumentation(builder)

       return DocumentationResult.documentation(builder.build())
    }
}