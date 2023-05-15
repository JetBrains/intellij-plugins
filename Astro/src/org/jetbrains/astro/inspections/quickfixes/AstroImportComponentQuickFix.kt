// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.inspections.quickfixes

import com.intellij.codeInsight.intention.FileModifier.SafeFieldForPreview
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.annotations.Nls
import org.jetbrains.astro.AstroBundle
import org.jetbrains.astro.editor.AstroComponentSourceEdit
import org.jetbrains.astro.lang.AstroFileImpl

class AstroImportComponentQuickFix(element: PsiElement,
                                   private val importName: String,
                                   elementToImport: PsiElement) : LocalQuickFixOnPsiElement(element) {

  @SafeFieldForPreview
  private val elementToImportPtr = elementToImport.createSmartPointer()

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getText(): String {
    return AstroBundle.message("astro.quickfix.import.component.name", importName)
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getFamilyName(): String {
    return AstroBundle.message("astro.quickfix.import.component.family")
  }

  override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
    val componentSourceEdit = AstroComponentSourceEdit(file as? AstroFileImpl ?: return)
    val elementToImport = elementToImportPtr.dereference() as? AstroFileImpl ?: return

    componentSourceEdit.insertAstroComponentImport(importName, elementToImport)
    componentSourceEdit.reformatChanges()
  }

}