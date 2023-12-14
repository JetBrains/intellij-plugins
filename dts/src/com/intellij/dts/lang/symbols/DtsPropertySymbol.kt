package com.intellij.dts.lang.symbols

import com.intellij.dts.documentation.DtsPropertyBindingDocumentationTarget
import com.intellij.dts.lang.DtsPropertyType
import com.intellij.dts.zephyr.binding.DtsPropertyValue
import com.intellij.dts.zephyr.binding.DtsZephyrPropertyBinding
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget

data class DtsPropertySymbol(private val binding: DtsZephyrPropertyBinding) : DtsDocumentationSymbol {
  val name: String get() = binding.name

  val type: DtsPropertyType get() = binding.type

  val defaultValue: DtsPropertyValue? get() = binding.default?.takeIf { binding.type in it.assignableTo }

  override fun createPointer(): Pointer<out Symbol> = Pointer { this }

  override fun getDocumentationTarget(project: Project): DocumentationTarget = DtsPropertyBindingDocumentationTarget(project, binding)
}