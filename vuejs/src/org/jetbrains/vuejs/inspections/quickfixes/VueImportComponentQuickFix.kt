// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.inspections.quickfixes

import com.intellij.codeInsight.intention.FileModifier.SafeFieldForPreview
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.annotations.Nls
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.editor.VueComponentSourceEdit
import org.jetbrains.vuejs.model.VueModelManager

class VueImportComponentQuickFix(element: PsiElement,
                                 private val importName: String,
                                 elementToImport: PsiElement) : LocalQuickFixOnPsiElement(element) {

  @SafeFieldForPreview
  private val elementToImportPtr = elementToImport.createSmartPointer()

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getText(): String {
    return VueBundle.message("vue.quickfix.import.component.name", importName)
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getFamilyName(): String {
    return VueBundle.message("vue.quickfix.import.component.family")
  }

  override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
    val componentSourceEdit = VueComponentSourceEdit.create(VueModelManager.findEnclosingContainer(startElement))
                              ?: return
    val elementToImport = elementToImportPtr.dereference() ?: return
    val importFile = elementToImportPtr.containingFile ?: return
    val adjustedElementToImport = if (importFile is XmlFile || ES6PsiUtil.findDefaultExports(importFile)
        .any { PsiTreeUtil.isContextAncestor(it, elementToImport, false) })
      importFile
    else elementToImport
    componentSourceEdit.insertComponentImport(importName, adjustedElementToImport)
    componentSourceEdit.reformatChanges()
  }

}