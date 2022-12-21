// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import org.angular2.lang.Angular2Bundle

class Angular2GotoDeclarationHandler : GotoDeclarationHandler {

  override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement>? {
    return null
  }

  override fun getActionText(context: DataContext): String? {
    val directives = Angular2EditorUtils.getDirectivesAtCaret(context)
    return if (!directives.isEmpty()) {
      if (directives.all { it.isComponent })
        Angular2Bundle.message("angular.action.goto-declaration.component")
      else
        Angular2Bundle.message("angular.action.goto-declaration.directive")
    }
    else null
  }
}
