// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.cssModules

import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.psi.PsiElement
import com.intellij.psi.css.*
import com.intellij.psi.impl.source.PsiFileWithStubSupport
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.MultiMap
import org.jetbrains.vuejs.model.VueImplicitElement

class CssModuleType private constructor(val container: PsiElement, source: JSTypeSource) : JSSimpleTypeBaseImpl(source), JSCodeBasedType {

  constructor(container: PsiElement, context: PsiElement?) : this(container, JSTypeSourceFactory.createTypeSource(context, true))

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType {
    val stylesheet = PsiTreeUtil.getStubChildOfType(
      (container as? PsiFileWithStubSupport)?.stubTree?.root?.psi ?: container,
      CssStylesheet::class.java) ?: return JSAnyType.get(source)

    val allClasses = MultiMap<String, PsiElement>()
    stylesheet.rulesets.forEach {
      processSelectors(it, allClasses)
    }
    val propertyType = JSStringType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE)

    return JSRecordTypeImpl(source, allClasses.entrySet().map { (name, sources) ->
      val source = JSRecordMemberSourceFactory.createSource(
        sources.map { VueImplicitElement(name, propertyType, it, JSImplicitElement.Type.Property, true) },
        JSRecordType.MemberSourceKind.Union)
      JSRecordTypeImpl.PropertySignatureImpl(name, propertyType, false, true, source)
    })
  }

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    CssModuleType(container, source)

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    type is CssModuleType && type.container == this.container

  override fun hashCodeImpl(): Int {
    return container.hashCode()
  }

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("css-module($container)")
      return
    }
    val evaluatedType = substitute()
    if (evaluatedType !== this) {
      evaluatedType.buildTypeText(format, builder)
    }
    else {
      builder.append(JSCommonTypeNames.ANY_TYPE_NAME)
    }
  }

  private fun detectOptions(): Options {
    // TODO Detect options from WebPack configuration - https://github.com/webpack-contrib/css-loader#object
    return Options(Mode.Local, false, Convention.AsIs)
  }

  private fun processSelectors(ruleset: CssRuleset, result: MultiMap<String, PsiElement>) {
    val options = detectOptions()
    fun addLocalClass(it: CssClass) {
      val name = it.name ?: return
      val processedName = options.exportLocalsConvention.format(name)
      result.putValue(processedName, it)
      if (processedName != name
          && options.exportLocalsConvention != Convention.CamelCaseOnly
          && options.exportLocalsConvention != Convention.DashesOnly) {
        result.putValue(name, it)
      }
    }

    fun addGlobalClass(it: CssClass) {
      result.putValue(it.name, it)
    }

    fun processSelectorsInPseudoClassArgs(pseudoClass: CssPseudoClass, add: (CssClass) -> Unit) {
      PsiTreeUtil.getChildOfType(pseudoClass, CssFunction::class.java)
        ?.let { PsiTreeUtil.getChildOfType(it, CssSelectorList::class.java) }
        ?.let { selectorList ->
          selectorList.selectors.forEach { selector ->
            selector.simpleSelectors.forEach { simpleSelector ->
              simpleSelector.selectorSuffixes.forEach { selectorSuffix ->
                (selectorSuffix as? CssClass)?.let { cssClass ->
                  add(cssClass)
                }
              }
            }
          }
        }
    }
    for (selector in ruleset.selectors) {
      var globalMode = options.defaultMode == Mode.Global
      for (simpleSelector in selector.simpleSelectors) {
        for (suffix in simpleSelector.selectorSuffixes) {
          when (suffix) {
            is CssClass -> if (!globalMode) addLocalClass(suffix) else if (options.exportGlobals) addGlobalClass(suffix)
            is CssPseudoClass -> {
              when (suffix.name) {
                "local" -> {
                  if (suffix.expressionText != null) {
                    processSelectorsInPseudoClassArgs(suffix, ::addLocalClass)
                  }
                  else {
                    globalMode = false
                  }
                }
                "global" -> {
                  if (suffix.expressionText != null) {
                    if (options.exportGlobals) {
                      processSelectorsInPseudoClassArgs(suffix, ::addGlobalClass)
                    }
                  }
                  else {
                    globalMode = true
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private class Options(val defaultMode: Mode, val exportGlobals: Boolean, val exportLocalsConvention: Convention)

  private enum class Mode {
    Local,
    Global,
    Pure
  }

  private enum class Convention {
    AsIs {
      override fun format(name: String): String = name
    },
    CamelCase {
      override fun format(name: String): String = JSStringUtil.toCamelCase(name)
    },
    CamelCaseOnly {
      override fun format(name: String): String = JSStringUtil.toCamelCase(name)
    },
    Dashes {
      override fun format(name: String): String =
        dashesOnlyReplacePattern.replace(name) { it.groupValues[0].toUpperCase() }
    },
    DashesOnly {
      override fun format(name: String): String =
        dashesOnlyReplacePattern.replace(name) { it.groupValues[0].toUpperCase() }
    };

    abstract fun format(name: String): String
  }

  companion object {
    private val dashesOnlyReplacePattern = Regex("-+(\\w)")
  }

}