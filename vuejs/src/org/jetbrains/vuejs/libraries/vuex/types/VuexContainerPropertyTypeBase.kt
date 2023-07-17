// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.types

import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.JSTypeWithIncompleteSubstitution
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSCodeBasedType
import com.intellij.lang.javascript.psi.types.JSSimpleTypeBaseImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.settings.JSSymbolPresentationProvider
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexModelManager
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStoreContext
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStoreNamespace
import java.util.*
import java.util.function.Function

abstract class VuexContainerPropertyTypeBase(source: JSTypeSource,
                                             protected val element: PsiElement,
                                             protected val baseNamespace: VuexStoreNamespace)
  : JSSimpleTypeBaseImpl(source), JSCodeBasedType, JSTypeWithIncompleteSubstitution {


  override fun hashCodeImpl(): Int {
    return Objects.hash(element, baseNamespace)
  }

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean {
    return (type.javaClass == this.javaClass)
           && (type as VuexContainerPropertyTypeBase).element == element
           && type.baseNamespace == baseNamespace
  }

  abstract val kind: String

  private val resolvedNamespace: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
    baseNamespace.get(element)
  }

  private fun appendPseudoType(builder: JSTypeTextBuilder, withNamespace: Boolean = true) {
    builder.append("/* Vuex ")
    if (resolvedNamespace.isEmpty())
      builder.append("store ${kind} */")
    else {
      builder.append("module ${kind}")
      if (withNamespace) {
        builder.append(" ['").append(resolvedNamespace).append("']")
      }
      builder.append(" */")
    }
  }

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format != JSType.TypeTextFormat.SIMPLE) {
      appendPseudoType(builder)
      val el = substituteCompletely()
      if (el is JSRecordType) {
        builder.append(" {\n")
        val useTypeScriptRecordTypeFormat = format == JSType.TypeTextFormat.CODE && isTypeScript
        val prefix = "    "
        val nextPrefix = if (useTypeScriptRecordTypeFormat) ";\n" else ",\n"
        val separator = getTypeSeparator(format)
        val quote = JSSymbolPresentationProvider.getDefaultQuote(source.sourceElement)[0]

        el.properties
          .sortedWith(Comparator.comparing(Function<JSRecordType.PropertySignature, Boolean> { it.jsType is VuexContainerPropertyTypeBase })
                        .thenComparing(Function<JSRecordType.PropertySignature, Int> { it.memberName.count { ch -> ch == '/' } })
                        .thenComparing(Function<JSRecordType.PropertySignature, String> { it.memberName }))
          .forEach { property ->
            builder.append(prefix)
            when (val jsType = property.jsType) {
              is VuexContainerPropertyTypeBase -> {
                if (property.isConst) builder.append("readonly ")
                val fixedName = JSSymbolUtil.quoteIfSpecialPropertyName(property.memberName, property.isPrivateName, quote)
                builder.append(fixedName)
                builder.append(separator)
                builder.append("object ")
                jsType.appendPseudoType(builder, false)
              }
              else -> property.appendMemberPresentation(format, builder, separator, quote)
            }
            builder.append(nextPrefix)
          }
        builder.append("}")
      }
      else {
        el.buildTypeText(format, builder)
      }
    }
    else {
      builder.append("${javaClass.simpleName} : ${baseNamespace} on element ${element}")
    }
  }

  override fun substituteCompletely(): JSType {
    return VuexModelManager.getVuexStoreContext(element)
             ?.let { createStateRecord(it, VuexStoreContext.appendSegment(resolvedNamespace, "")) }
           ?: JSAnyType.getWithLanguage(source.language, false)
  }

  abstract fun createStateRecord(context: VuexStoreContext, baseNamespace: String): JSRecordType?

}
