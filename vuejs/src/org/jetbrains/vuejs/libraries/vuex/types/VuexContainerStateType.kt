// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.types

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.JSTypeWithIncompleteSubstitution
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.*
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexContainer
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexModelManager
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexModule
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStoreContext
import java.util.*
import java.util.function.Function

class VuexContainerStateType private constructor(source: JSTypeSource,
                                                 private val element: PsiElement,
                                                 private val baseNamespace: String)
  : JSSimpleTypeBaseImpl(source), JSCodeBasedType, JSTypeWithIncompleteSubstitution {

  constructor(element: PsiElement, baseNamespace: String)
    : this(JSTypeSource(element.containingFile, element, JSTypeSource.SourceLanguage.TS, true), element, baseNamespace)

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return VuexContainerStateType(source, element, baseNamespace)
  }

  override fun resolvedHashCodeImpl(): Int {
    return Objects.hash(element, baseNamespace)
  }

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean {
    return (type as VuexContainerStateType).element == element
           && type.baseNamespace == baseNamespace
  }

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    builder.append("Vuex")
    if (baseNamespace.isEmpty())
      builder.append("StoreState")
    else
      builder.append("ModuleState<'").append(baseNamespace).append("'>")
    if (format != JSType.TypeTextFormat.SIMPLE) {
      val el = substituteCompletely()
      if (el is JSRecordType) {
        builder.append(" {\n")
        el.properties
          .sortedWith(Comparator.comparing(Function<JSRecordType.PropertySignature, Boolean> { it.jsType is VuexContainerStateType })
                        .thenComparing(Function<JSRecordType.PropertySignature, String> { it.memberName }))
          .forEach { property ->
            builder.append("    \"").append(property.memberName).append("\": ")
            val jsType = property.jsType
            when {
              jsType is VuexContainerStateType ->
                builder.append(jsType, JSType.TypeTextFormat.SIMPLE)
              jsType != null -> builder.append(jsType, format)
              else -> builder.append("any")
            }
            builder.append(",\n")
          }
        builder.append("}")
      }
      else {
        el.buildTypeText(format, builder)
      }
    }
  }

  override fun substituteCompletely(): JSType {
    return createStateRecord()
           ?: JSAnyType.getWithLanguage(source.language, false)
  }

  private fun createStateRecord(): JSRecordType? {
    val baseNamespace = VuexStoreContext.appendSegment(baseNamespace, "")
    val context = VuexModelManager.getVuexStoreContext(element) ?: return null
    val result = mutableListOf<JSRecordType.TypeMember>()
    val prefixLength = baseNamespace.length
    context.visit { namespace, container ->
      if (container is VuexModule
          && namespace.startsWith(baseNamespace)
          && namespace.length > baseNamespace.length
          && namespace.indexOf('/', prefixLength + 1) < 0) {
        val name = namespace.substring(prefixLength)
        val type = VuexContainerStateType(source, element, namespace)
        result.add(JSRecordTypeImpl.PropertySignatureImpl(
          name, type, false, true,
          JSLocalImplicitElementImpl(name, type, container.source, JSImplicitElement.Type.Property)))
      }
    }
    context.visitSymbols(VuexContainer::state) { fullName, symbol ->
      if (fullName.startsWith(baseNamespace)
          && fullName.indexOf('/', prefixLength + 1) < 0) {
        result.add(JSRecordTypeImpl.PropertySignatureImpl(
          symbol.name, symbol.jsType, false, false, symbol.resolveTarget))
      }
    }
    return JSSimpleRecordTypeImpl(source, result)
  }
}
