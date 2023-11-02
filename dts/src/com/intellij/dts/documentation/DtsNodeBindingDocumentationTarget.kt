package com.intellij.dts.documentation

import com.intellij.dts.DtsIcons
import com.intellij.dts.zephyr.binding.DtsZephyrBinding
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import org.jetbrains.annotations.Nls

class DtsNodeBindingDocumentationTarget(
  project: Project,
  private val name: @Nls String,
  private val binding: DtsZephyrBinding,
) : DtsDocumentationTarget(project) {
    override fun createPointer(): Pointer<out DocumentationTarget> = Pointer { this }

    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder(name).icon(DtsIcons.Node).presentation()
    }

    override fun buildDocumentation(html: DtsDocumentationHtmlBuilder) {
        buildNodeName(html, name)
        buildNodeBinding(html, binding)
    }
}