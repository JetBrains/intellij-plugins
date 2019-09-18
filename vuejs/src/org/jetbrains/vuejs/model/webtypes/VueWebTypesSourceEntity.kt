// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.model.webtypes.json.Source

open class VueWebTypesSourceEntity(protected val project: Project,
                                   private val sourceInfo: Source?,
                                   private val sourceSymbolResolver: WebTypesSourceSymbolResolver): UserDataHolderBase() {

  val source: PsiElement?
    get() {
      return sourceInfo?.let {
        CachedValuesManager.getManager(project).getCachedValue(this) {
          sourceSymbolResolver.resolve(it)
        }
      }
    }

}
