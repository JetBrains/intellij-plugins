// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.impl

import com.intellij.lang.documentation.DocumentationResult
import com.intellij.lang.documentation.DocumentationTarget
import com.intellij.lang.typescript.documentation.TypeScriptDocumentationProvider
import com.intellij.model.Pointer
import com.intellij.navigation.TargetPresentation
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer

class TypeScriptElementDocumentationTarget(@NlsSafe val name: String,
                                           val element: PsiElement) : DocumentationTarget {

  override fun createPointer(): Pointer<out DocumentationTarget> {
    val name = this.name
    val elementPtr = this.element.createSmartPointer()
    return Pointer {
      val element = elementPtr.dereference() ?: return@Pointer null
      TypeScriptElementDocumentationTarget(name, element)
    }
  }

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(name).presentation()

  override fun computeDocumentation(): DocumentationResult? =
    TypeScriptDocumentationProvider().generateDoc(element, null)
      ?.let { DocumentationResult.documentation(it) }
}