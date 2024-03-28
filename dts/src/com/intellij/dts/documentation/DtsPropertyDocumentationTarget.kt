package com.intellij.dts.documentation

import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.createSmartPointer

class DtsPropertyDocumentationTarget(private val property: DtsProperty) : DtsDocumentationTarget(property.project) {
  override fun createPointer(): Pointer<out DocumentationTarget> {
    val propertyPtr = property.createSmartPointer()

    return Pointer {
      propertyPtr.dereference()?.let { DtsPropertyDocumentationTarget(it) }
    }
  }

  override fun computePresentation(): TargetPresentation {
    return TargetPresentation.builder(property.dtsName).icon(property.getIcon(0)).presentation()
  }

  override fun buildDocumentation(html: DtsDocumentationHtmlBuilder) {
    buildPropertyName(html, property.dtsName)

    val parent = DtsTreeUtil.parentNode(property) ?: return
    buildDeclaredIn(html, parent)

    val binding = DtsZephyrBindingProvider.bindingFor(parent) ?: return
    val propertyBinding = binding.properties[property.dtsName] ?: return
    buildPropertyBinding(html, propertyBinding)
  }
}