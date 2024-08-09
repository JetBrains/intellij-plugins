// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.hints

import com.intellij.codeInsight.hints.VcsCodeVisionLanguageContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLFile
import java.awt.event.MouseEvent

internal class TfVcsContextProvider : VcsCodeVisionLanguageContext {
  override fun isAccepted(element: PsiElement): Boolean = element is HCLBlock && element.parent is HCLFile

  override fun handleClick(mouseEvent: MouseEvent, editor: Editor, element: PsiElement) {}
}