package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag

/**
 * @author Irina.Chernushina on 12/11/2017.
 */
class VueGotoDeclarationHandler: GotoDeclarationHandler {
  override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor?): Array<PsiElement>? {
    val tag = sourceElement?.parent as? XmlTag ?: return null
    return (VueTagProvider().getDescriptor(tag) as? VueElementDescriptor)?.variants?.toTypedArray()
  }

  override fun getActionText(context: DataContext?): String? = null
}