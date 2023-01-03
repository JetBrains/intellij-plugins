// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.primitives.TypeScriptNeverJSTypeImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.entities.Angular2ComponentLocator.findComponentClass
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
      type
    )
    return if (result.isEmpty()) {
      null
    }
    else JSCompositeTypeImpl.getCommonType(result, type.source, false)
  }

  fun getTemplateBindingsContextType(bindings: Angular2TemplateBindings): JSType? {
    return BindingsTypeResolver.get(bindings).resolveTemplateContextType()
  }

  fun getNgTemplateTagContextType(tag: XmlTag): JSType? {
    return if (isTemplateTag(tag)) CachedValuesManager.getCachedValue(tag) {
      var result: JSType? = BindingsTypeResolver.get(tag).resolveTemplateContextType()
      if (result is TypeScriptNeverJSTypeImpl) {
        result = null
      }
      CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
    else null
  }

  fun createJSTypeSourceForXmlElement(context: PsiElement): JSTypeSource {
    return JSTypeSourceFactory.createTypeSource(findComponentClass(context) ?: context, true)
  }

  fun getElementEventMap(typeSource: JSTypeSource): JSType {
    // A generic parameter type should be extracted from addListener method of tag element interface, which looks like:
    // addEventListener<K extends keyof HTMLElementEventMap>(...): void;
    // However, all additional properties are anyway covered by `on*` methods,
    // so we can default to HTMLElementEventMapInterface, for events like `focusin`, which is not
    // covered by an `on*` method
    return JSNamedTypeFactory.createType(HTML_ELEMENT_EVENT_MAP_INTERFACE_NAME, typeSource, JSContext.INSTANCE)
  }
}