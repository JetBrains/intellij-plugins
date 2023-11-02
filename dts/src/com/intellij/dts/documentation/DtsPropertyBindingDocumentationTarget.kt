package com.intellij.dts.documentation

import com.intellij.dts.DtsIcons
import com.intellij.dts.zephyr.binding.DtsZephyrPropertyBinding
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation

class DtsPropertyBindingDocumentationTarget(
  project: Project,
  private val binding: DtsZephyrPropertyBinding,
) : DtsDocumentationTarget(project) {
    override fun createPointer(): Pointer<out DocumentationTarget> = Pointer { this }

    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder(binding.name).icon(DtsIcons.Property).presentation()
    }

    override fun buildDocumentation(html: DtsDocumentationHtmlBuilder) {
        buildPropertyName(html, binding.name)
        buildPropertyBinding(html, binding)
    }
}