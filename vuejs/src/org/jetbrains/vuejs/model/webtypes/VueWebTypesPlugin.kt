// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.BOOLEAN_TYPE
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.json.Html
import org.jetbrains.vuejs.model.webtypes.json.WebTypes
import java.util.*

class VueWebTypesPlugin(webTypes: WebTypes) : VuePlugin {
  override val source: PsiElement? = null
  override val global: VueGlobal? = null
  override val parents: List<VueEntitiesContainer> = emptyList()

  override val components: Map<String, VueComponent>
  override val directives: Map<String, VueDirective>

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
        { type: Any? -> null }

    components = webTypes.contributions
                   ?.html
                   ?.tags
                   ?.filter { it.name != null }
                   ?.associateBy({ it.name!! }, { VueWebTypesComponent(it, this, typeProvider) })
                 ?: Collections.emptyMap()
    directives = webTypes.contributions
                   ?.html
                   ?.attributes
                   ?.filter { it.name?.startsWith("v-") ?: false }
                   ?.associateBy({ it.name!!.substring(2) }, { VueWebTypesDirective(it, this) })
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
