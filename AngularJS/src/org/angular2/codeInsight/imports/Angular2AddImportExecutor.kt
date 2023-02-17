// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.ecmascript6.actions.ES6AddImportExecutor
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import org.angular2.entities.Angular2ComponentLocator

class Angular2AddImportExecutor(place: PsiElement) : ES6AddImportExecutor(place) {
  override fun prepareScopeToAdd(place: PsiElement, fromExternalModule: Boolean): PsiElement? {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    return Angular2ComponentLocator.findComponentClass(place)?.containingFile
  }

  override fun postProcessScope(place: PsiElement, info: JSImportDescriptor, scope: PsiElement) {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val componentClass = Angular2ComponentLocator.findComponentClass(place) ?: return
    val anchor = componentClass.lastChild
    runUndoTransparentWriteAction {
      PsiDocumentManager.getInstance(place.project).commitAllDocuments()
      val semicolon = JSCodeStyleSettings.getSemicolon(componentClass)
      JSChangeUtil.createClassMemberPsiFromTextWithContext("protected readonly ${info.importedName} = ${info.importedName}$semicolon",
                                                           componentClass, JSElement::class.java)?.let { member ->
        val inserted = componentClass.addBefore(member, anchor)
        CodeStyleManager.getInstance(place.project).reformatNewlyAddedElement(componentClass.node, inserted.node)
      }
    }
  }

  override fun createImportOrUpdateExistingInner(descriptor: JSImportDescriptor) {
    val type = descriptor.importType
    if (type.isComposite || descriptor !is Angular2GlobalImportCandidateDescriptor) {
      super.createImportOrUpdateExistingInner(descriptor)
    }
    else {
      val scope = prepareScopeToAdd(place, !type.isNamespace) ?: return
      postProcessScope(place, descriptor, scope)
    }
  }

  override fun getImportStatementText(descriptor: JSImportDescriptor): String {
    if (descriptor is Angular2GlobalImportCandidateDescriptor) {
      return "import ${descriptor.name}"
    }
    return super.getImportStatementText(descriptor)
  }

}