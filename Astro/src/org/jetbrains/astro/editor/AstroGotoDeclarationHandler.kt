// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.editor

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.typescript.getNavigationFromService
import com.intellij.openapi.editor.Editor
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.util.concurrency.ThreadingAssertions
import org.jetbrains.astro.context.isAstroFrameworkContext

class AstroGotoDeclarationHandler : GotoDeclarationHandler {
  override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement>? {
    ThreadingAssertions.assertReadAccess()
    val element = sourceElement ?: return null
    if (!isAstroFrameworkContext(element)) return null

    val project = editor.project ?: return null
    val targets = getNavigationFromService(project, element, editor) ?: return null
    return targets.map { if (it is Navigatable && it.canNavigate()) it else it.containingFile }.toTypedArray()
  }
}
