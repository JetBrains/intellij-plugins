// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.*
import org.angular2.entities.source.Angular2SourceDirective.Companion.readDirectivePropertyMappings

class Angular2SourceHostDirectiveWithMappings(private val definition: JSObjectLiteralExpression) : Angular2HostDirective {
  override val directive: Angular2Directive?
    get() = cachedInfo.first

  override val bindings: Angular2DirectiveProperties
    get() = cachedInfo.second

  private val cachedInfo: Pair<Angular2Directive?, Angular2DirectiveProperties>
    get() =
      definition.let { def ->
        CachedValuesManager.getCachedValue(def) {
          val directive = def.findProperty(Angular2DecoratorUtil.DIRECTIVE_PROP)
            ?.jsType
            ?.substitute()
            ?.sourceElement
            ?.asSafely<TypeScriptClass>()
            ?.let { Angular2EntitiesProvider.getDirective(it) }
          CachedValueProvider.Result.create(
            Pair(directive, calculateProperties(directive, def)),
            PsiModificationTracker.MODIFICATION_COUNT)
        }
      }

  override fun equals(other: Any?): Boolean =
    other === this
    || (other is Angular2SourceHostDirectiveWithMappings
        && other.definition == definition)

  override fun hashCode(): Int =
    definition.hashCode()

  companion object {

    fun createHostDirectiveProperties(directive: Angular2Directive?, inputsMap: Map<String, String>, outputsMap: Map<String, String>): Angular2DirectiveProperties {
      if (directive == null)
        return Angular2DirectiveProperties(emptyList(), emptyList())
      val originalBindings = directive.bindings
      return Angular2DirectiveProperties(
        originalBindings.inputs.mapNotNull { createHostProperty(it, inputsMap) } + directive.hostDirectives.flatMap { it.inputs },
        originalBindings.outputs.mapNotNull { createHostProperty(it, outputsMap) } + directive.hostDirectives.flatMap { it.outputs }
      )
    }
    private fun calculateProperties(directive: Angular2Directive?, def: JSObjectLiteralExpression): Angular2DirectiveProperties {
      val inputMap = readDirectivePropertyMappings(def.findProperty(Angular2DecoratorUtil.INPUTS_PROP))
      val outputMap = readDirectivePropertyMappings(def.findProperty(Angular2DecoratorUtil.OUTPUTS_PROP))
      return createHostDirectiveProperties(directive, inputMap, outputMap)
    }

    private fun createHostProperty(property: Angular2DirectiveProperty, map: Map<String, String>): Angular2DirectiveProperty? {
      val mappedName = map[property.name] ?: return null
      if (mappedName == property.name) return property
      return Angular2AliasedDirectiveProperty(property, mappedName)
    }
  }
}