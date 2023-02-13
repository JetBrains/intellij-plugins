// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.imports

import com.intellij.lang.ecmascript6.actions.ES6AddImportExecutor
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.psi.util.PsiEditorUtil
import org.jetbrains.vuejs.editor.VueComponentSourceEdit
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueRegularComponent
import org.jetbrains.vuejs.model.source.COMPUTED_PROP
import org.jetbrains.vuejs.model.source.METHODS_PROP

class VueAddImportExecutor(place: PsiElement) : ES6AddImportExecutor(place) {

  override fun prepareScopeToAdd(place: PsiElement, fromExternalModule: Boolean): PsiElement? {
    if (place !is JSReferenceExpression) return null
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val component = VueModelManager.findEnclosingContainer(place) as? VueRegularComponent ?: return null
    val componentEdit = VueComponentSourceEdit.create(component) ?: return null
    val project = place.project
    return runUndoTransparentWriteAction {
      val manager = PsiDocumentManager.getInstance(project)
      val document = manager.getDocument(place.containingFile)
      if (document != null) {
        manager.commitDocument(document)
      }
      componentEdit.getOrCreateScriptScope()
    }
  }

  override fun postProcessScope(place: PsiElement, info: JSImportDescriptor, scope: PsiElement) {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val component = VueModelManager.findEnclosingContainer(place) as? VueRegularComponent ?: return
    val componentEdit = VueComponentSourceEdit.create(component) ?: return

    if (!componentEdit.isScriptSetup()) {
      runUndoTransparentWriteAction {
        PsiDocumentManager.getInstance(place.project).commitAllDocuments()
        val element = JSStubBasedPsiTreeUtil.resolveLocally(info.effectiveName, scope) ?: return@runUndoTransparentWriteAction
        val type = JSStubBasedPsiTreeUtil.calculateMeaningfulElements(element).firstOrNull()
          ?.let { JSResolveUtil.getElementJSType(it) }
          ?.substitute()

        @Suppress("DEPRECATION")
        val editor = PsiEditorUtil.findEditor(scope)
          ?.let { InjectedLanguageUtil.getInjectedEditorForInjectedFile(it, place.containingFile) }
        val shouldPossiblyMoveCursor = editor != null && editor.caretModel.offset == place.textRange.endOffset
        if (type != null && JSTypeUtils.hasFunctionType(type, false, element)) {
          componentEdit.addClassicPropertyReference(METHODS_PROP, info.effectiveName)
        }
        else if (type == null || JSTypeUtils.isInstanceType(type)) {
          componentEdit.addClassicPropertyFunction(METHODS_PROP, info.effectiveName, "return ${info.effectiveName}")
          JSChangeUtil.createExpressionPsiWithContext("${info.effectiveName}()", place, JSCallExpression::class.java)
            ?.let { place.replace(it) }
          PsiDocumentManager.getInstance(place.project).commitAllDocuments()

          if (editor != null && shouldPossiblyMoveCursor) {
            editor.caretModel.moveCaretRelatively(2, 0, false, false, false)
          }
        }
        else {
          componentEdit.addClassicPropertyFunction(COMPUTED_PROP, info.effectiveName, "return ${info.effectiveName}")
        }
      }
    }
  }

}