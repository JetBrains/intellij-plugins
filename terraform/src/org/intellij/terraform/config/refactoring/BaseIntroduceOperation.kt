// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

open class BaseIntroduceOperation<T : PsiElement>(val project: Project,
                                                  val editor: Editor,
                                                  val file: PsiFile,
                                                  var name: String?) {
  var isReplaceAll: Boolean = false
  var element: PsiElement? = null
  var initializer: T? = null
  var occurrences: List<PsiElement> = emptyList()
  var suggestedNames: Collection<String>? = null
}