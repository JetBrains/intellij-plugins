// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.editor

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.codeInsight.tags.VueElementDescriptor

class VueGotoDeclarationHandler : GotoDeclarationHandler {
  override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor?): Array<PsiElement>? {
    return ((sourceElement?.parent as? XmlTag)?.descriptor as? VueElementDescriptor)
      ?.getPsiSources()
      ?.toTypedArray()
  }
}
