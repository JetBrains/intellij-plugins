// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.refactoring

import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.resolve

object VueRefactoringUtils {
  fun getComponent(element: PsiElement): JSImplicitElement? {
    if (element is JSImplicitElementImpl) {
      return resolve(element.name, GlobalSearchScope.projectScope(element.project), VueComponentsIndex.KEY)?.first()
    }
    return null
  }

  fun isComponentName(element: PsiElement): Boolean {
    if (element.containingFile == null) return false
    if (element.containingFile.fileType != VueFileType.INSTANCE) return false
    return ((element.parent.parent as? JSPropertyImpl)?.name == "name")
  }
}