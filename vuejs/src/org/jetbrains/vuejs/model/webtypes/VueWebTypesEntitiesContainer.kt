// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.jetbrains.vuejs.codeInsight.BOOLEAN_TYPE
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.json.Html
import org.jetbrains.vuejs.model.webtypes.json.WebTypes
import java.util.*

open class VueWebTypesEntitiesContainer(project: Project, packageJson: VirtualFile,
                                        webTypes: WebTypes, owner: VueEntitiesContainer) : VueEntitiesContainer {

  override val source: PsiElement? = null
  override val parents: List<VueEntitiesContainer> = emptyList()
  final override val components: Map<String, VueComponent>
  final override val directives: Map<String, VueDirective>

  override val filters: Map<String, VueFilter>
    get() = Collections.emptyMap()
  override val mixins: List<VueMixin>
    get() = Collections.emptyList()

  init {
    assert(webTypes.framework == WebTypes.Framework.VUE)

    val typeProvider: (Any?) -> JSType? =
      if (webTypes.contributions?.html?.typesSyntax == Html.TypesSyntax.TYPESCRIPT)
        TypeScriptTypeProvider()::getType
      else
        { _: Any? -> null }
    val psiPackageJson = PsiManager.getInstance(project).findFile(packageJson)
    val sourceSymbolResolver = WebTypesSourceSymbolResolver(psiPackageJson!!, webTypes.name ?: "unknown")

    components = webTypes.contributions
                   ?.html
                   ?.tags
                   ?.filter { it.name != null }
                   ?.associateBy({ it.name!! }, { VueWebTypesComponent(it, owner, typeProvider, sourceSymbolResolver) })
                 ?: Collections.emptyMap()
    directives = webTypes.contributions
                   ?.html
                   ?.attributes
                   ?.filter { it.name?.startsWith("v-") ?: false }
                   ?.associateBy({ it.name!!.substring(2) }, { VueWebTypesDirective(it, owner, sourceSymbolResolver) })
                 ?: Collections.emptyMap()
  }

  private class TypeScriptTypeProvider {
    fun getType(type: Any?): JSType? {
      if (type is String) {
        return getType(type)
      }
      else if (type is List<*>) {
        val result = type.mapNotNull { t -> if (t is String) getType(t) else null }
        if (result.isNotEmpty()) {
          return JSCompositeTypeImpl.getCommonType(result, JSTypeSource.EXPLICITLY_DECLARED, false)
        }
      }
      return null
    }

    private fun getType(type: String): JSType? {
      // TODO support other types here
      if (type == "boolean") {
        return BOOLEAN_TYPE
      }
      return null
    }
  }

}
