// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP
import org.angular2.entities.*
import org.angular2.entities.source.Angular2SourceDirective.Companion.readDirectivePropertyMappings
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_INPUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_OUTPUTS

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

    fun createHostDirectiveProperties(directive: Angular2Directive?,
                                      inputsMap: MutableMap<String, Angular2PropertyInfo>,
                                      outputsMap: MutableMap<String, Angular2PropertyInfo>): Angular2DirectiveProperties {
      val originalBindings = directive?.bindings

      val inputs = originalBindings?.inputs?.mapNotNullTo(mutableListOf()) { createHostProperty(directive, it, inputsMap) }
                   ?: mutableListOf()
      val outputs = originalBindings?.outputs?.mapNotNullTo(mutableListOf()) { createHostProperty(directive, it, outputsMap) }
                    ?: mutableListOf()

      inputsMap.mapNotNullTo(inputs) {(_, info) ->
        if (info.declaringElement == null) return@mapNotNullTo null
        Angular2SourceDirectiveVirtualProperty(directive?.typeScriptClass, KIND_NG_DIRECTIVE_INPUTS, info.name,
                                               info.required, info.declaringElement, info.declarationRange)
      }
      outputsMap.mapNotNullTo(outputs) {(_, info) ->
        if (info.declaringElement == null) return@mapNotNullTo null
        Angular2SourceDirectiveVirtualProperty(directive?.typeScriptClass, KIND_NG_DIRECTIVE_OUTPUTS, info.name,
                                               info.required, info.declaringElement, info.declarationRange)
      }
      return Angular2DirectiveProperties(inputs.toList(), outputs.toList())
    }

    private fun calculateProperties(directive: Angular2Directive?, def: JSObjectLiteralExpression): Angular2DirectiveProperties {
      val inputMap = readDirectivePropertyMappings(def.findProperty(INPUTS_PROP))
      val outputMap = readDirectivePropertyMappings(def.findProperty(OUTPUTS_PROP))
      return createHostDirectiveProperties(directive, inputMap, outputMap)
    }

    private fun createHostProperty(directive: Angular2Directive,
                                   property: Angular2DirectiveProperty,
                                   map: MutableMap<String, Angular2PropertyInfo>): Angular2DirectiveProperty? {
      val mapping = map.remove(property.name) ?: return null
      if (mapping.name == property.name && mapping.declarationRange == null)
        return property
      return Angular2AliasedDirectiveProperty(directive, property, mapping.name,
                                              mapping.declaringElement ?: directive.sourceElement, mapping.declarationRange)
    }
  }
}