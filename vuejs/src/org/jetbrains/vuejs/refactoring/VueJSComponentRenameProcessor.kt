// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.refactoring

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.refactoring.JSDefaultRenameProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.html.HtmlTagImpl
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenameUtil
import com.intellij.usageView.UsageInfo
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.index.VueComponentsIndex

/**
 * @author Artem.Gainanov
 */

class VueJSComponentRenameProcessor : JSDefaultRenameProcessor() {


  override fun renameElement(element: PsiElement, newName: String, usages: Array<out UsageInfo>, listener: RefactoringElementListener?) {
    for (usage in usages) {
      if (usage.element is HtmlTagImpl && toAsset((usage.element as HtmlTagImpl).name) != (usage.element as HtmlTagImpl).name) {
        RenameUtil.rename(usage, fromAsset(newName))
      }
      else {
        RenameUtil.rename(usage, newName)
      }
    }
  }

  override fun findReferences(element: PsiElement, searchInCommentsAndStrings: Boolean): MutableCollection<PsiReference> {
    return ReferencesSearch.search(element).findAll()
  }

  override fun canProcessElement(element: PsiElement): Boolean {
    return VueRefactoringUtils.isComponentName(element)
  }

  override fun substituteElementToRename(element: PsiElement, editor: Editor?): PsiElement? {
    return element as? JSImplicitElement ?: JSImplicitElementImpl.Builder(element.text, element)
      .setUserString(VueComponentsIndex.JS_KEY)
      .toImplicitElement()
  }

  class VueJSComponentElementDescriptor : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
      when {
        VueRefactoringUtils.isComponentName(element) -> return "component"
        else -> return null
      }
    }
  }
}