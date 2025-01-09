// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.primitives.TypeScriptNeverType
import com.intellij.lang.javascript.psi.types.typescript.TypeScriptCompilerType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.expr.psi.Angular2TemplateBindings

object Angular2TypeUtils {
  private const val HTML_ELEMENT_EVENT_MAP_INTERFACE_NAME: String = "HTMLElementEventMap"
  fun extractEventVariableType(type: JSType?): JSType? {
    if (type == null) {
      return null
    }
    val result: MutableList<JSType?> = ArrayList()
    JSTypeUtils.processExpandedType(
      {
        when (it) {
          is JSGenericTypeImpl -> {
            val arguments = it.arguments
            if (arguments.size == 1) {
              result.add(arguments[0])
            }
            false
          }
          is JSFunctionType -> {
            val params = it.parameters
            if (params.size == 1 && !params[0].isRest) {
              result.add(params[0].simpleType)
            }
            false
          }
          else -> true
        }
      },
      type.let { if (it is TypeScriptCompilerType) it.substitute() else it }
    )
    return if (result.isEmpty()) {
      null
    }
    else JSCompositeTypeFactory.getCommonType(result, type.source, false)
  }

  fun getTemplateBindingsContextType(bindings: Angular2TemplateBindings): JSType? {
    return BindingsTypeResolver.get(bindings).resolveTemplateContextType()
  }

  fun getNgTemplateTagContextType(tag: XmlTag): JSType? {
    return if (isTemplateTag(tag)) CachedValuesManager.getCachedValue(tag) {
      var result: JSType? = BindingsTypeResolver.get(tag).resolveTemplateContextType()
      if (result is TypeScriptNeverType) {
        result = null
      }
      CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
    else null
  }

  fun createJSTypeSourceForXmlElement(context: PsiElement): JSTypeSource {
    return JSTypeSourceFactory.createTypeSource(Angular2EntitiesProvider.findTemplateComponent(context)?.jsResolveScope ?: context, true)
  }

  fun getElementEventMap(typeSource: JSTypeSource): JSType {
    // A generic parameter type should be extracted from addListener method of tag element interface, which looks like:
    // addEventListener<K extends keyof HTMLElementEventMap>(...): void;
    // However, all additional properties are anyway covered by `on*` methods,
    // so we can default to HTMLElementEventMapInterface, for events like `focusin`, which is not
    // covered by an `on*` method
    return JSNamedTypeFactory.createType(HTML_ELEMENT_EVENT_MAP_INTERFACE_NAME, typeSource, JSContext.INSTANCE)
  }

  fun buildTypeFromClass(cls: JSClass, context: PsiElement = cls): JSRecordType =
    withTypeEvaluationLocation(context) {
      TypeScriptTypeParser.buildTypeFromClass(cls, false)
    }

  val TypeScriptClass.possiblyGenericJsType: JSType
    get() = if (typeParameters.isNotEmpty())
      JSTypeUtils.createNotSubstitutedGenericType(this, jsType)
    else
      jsType
}