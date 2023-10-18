package com.intellij.dts.documentation

import com.intellij.dts.DtsBundle
import com.intellij.dts.util.DtsHtmlChunk
import com.intellij.dts.zephyr.DtsZephyrBinding
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation

class DtsBindingDocumentation(
    project: Project,
    private val binding: DtsZephyrBinding,
) : DtsDocumentationTarget(project) {
    override fun createPointer(): Pointer<out DocumentationTarget> = Pointer { this }

    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder(DtsBundle.message("documentation.binding")).presentation()
    }

    override fun buildDocumentation(html: DtsDocumentationHtmlBuilder) {
        binding.compatible?.let { compatible ->
            html.definition(DtsHtmlChunk.definitionName("documentation.compatible"))
            html.appendToDefinition(HtmlChunk.text(compatible))
        }
        binding.path?.let { path ->
            html.definition(DtsHtmlChunk.definitionName("documentation.path"))
            html.appendToDefinition(HtmlChunk.text(path))
        }
        binding.description?.let { description ->
            html.content(DtsHtmlChunk.binding(project, description))
        }
    }
}