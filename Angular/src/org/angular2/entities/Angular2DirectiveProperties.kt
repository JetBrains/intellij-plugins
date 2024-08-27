// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
import com.intellij.webSymbols.search.WebSymbolSearchTarget
import com.intellij.webSymbols.utils.coalesceWith
import org.angular2.codeInsight.documentation.Angular2ElementDocumentationTarget
import org.angular2.entities.source.Angular2SourceDirectiveProperty
import org.angular2.lang.Angular2LangUtil.OUTPUT_CHANGE_SUFFIX
import org.angular2.web.*
import java.util.*

class Angular2DirectiveProperties(rawInputs: Collection<Angular2DirectiveProperty>,
                                  rawOutputs: Collection<Angular2DirectiveProperty>,
                                  rawInOuts: Collection<Angular2DirectiveProperty>) {

  constructor(
    inputs: Collection<Angular2DirectiveProperty>,
    outputs: Collection<Angular2DirectiveProperty>
  ): this(inputs, outputs, emptyList())

  val inputs: Collection<Angular2DirectiveProperty> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    if (rawInOuts.isEmpty()) {
      return@lazy Collections.unmodifiableCollection(rawInputs)
    }
    rawInOuts.map { InputDirectiveProperty(it) } + rawInputs
  }

  val outputs: Collection<Angular2DirectiveProperty> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    if (rawInOuts.isEmpty()) {
      return@lazy Collections.unmodifiableCollection(rawOutputs)
    }
    rawInOuts.map { OutputDirectiveProperty(it) } + rawOutputs
  }

  val inOuts: Collection<Angular2Symbol> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    if (rawInputs.isEmpty() || rawOutputs.isEmpty()) {
      return@lazy Collections.unmodifiableCollection(rawInOuts)
    }
    val inputMap = HashMap<String, Angular2DirectiveProperty>()
    val result = ArrayList<Angular2Symbol>()
    for (p in rawInputs) {
      inputMap.putIfAbsent(p.name, p)
    }
    for (output in rawOutputs) {
      val name = output.name
      if (output.name.endsWith(OUTPUT_CHANGE_SUFFIX)) {
        val input = inputMap[name.substring(0, name.length - OUTPUT_CHANGE_SUFFIX.length)]
        if (input != null) {
          result.add(InOutDirectiveProperty(input, output))
        }
      }
    }
    result + rawInOuts
  }

  private class InputDirectiveProperty(inOut: Angular2DirectiveProperty)
    : AbstractFromInOutDirectiveProperty(inOut) {

    override val qualifiedKind: WebSymbolQualifiedKind
      get() = NG_DIRECTIVE_INPUTS

    override val required: Boolean
      get() = delegate.required

    override fun createPointer(): Pointer<InputDirectiveProperty> {
      val inOut = delegate.createPointer()
      return Pointer {
        inOut.dereference()?.let { InputDirectiveProperty(it) }
      }
    }
  }


  private class OutputDirectiveProperty(inOut: Angular2DirectiveProperty)
    : AbstractFromInOutDirectiveProperty(inOut) {

    override val name: String =
      inOut.name + OUTPUT_CHANGE_SUFFIX

    override val qualifiedKind: WebSymbolQualifiedKind
      get() = NG_DIRECTIVE_OUTPUTS

    override val required: Boolean
      get() = false

    override val type: JSType?
      get() = (delegate as? Angular2SourceDirectiveProperty)?.typeFromSignal
              ?: super.type

    override fun createPointer(): Pointer<InputDirectiveProperty> {
      val inOut = delegate.createPointer()
      return Pointer {
        inOut.dereference()?.let { InputDirectiveProperty(it) }
      }
    }

  }

  private abstract class AbstractFromInOutDirectiveProperty(inOut: Angular2DirectiveProperty)
    : Angular2SymbolDelegate<Angular2DirectiveProperty>(inOut),
      Angular2DirectiveProperty, PsiSourcedWebSymbol {

    override val psiContext: PsiElement?
      get() = delegate.psiContext

    override val source: PsiElement?
      get() = (delegate as? PsiSourcedWebSymbol)?.source

    override val rawJsType: JSType?
      get() = delegate.rawJsType

    override val sourceElement: PsiElement
      get() = delegate.sourceElement

    abstract override val required: Boolean

    override val type: JSType?
      get() = delegate.type

    override val searchTarget: WebSymbolSearchTarget?
      get() = delegate.searchTarget

    override val apiStatus: WebSymbolApiStatus
      get() = delegate.apiStatus

    override val fieldName: String?
      get() = delegate.fieldName

    override val project: Project
      get() = delegate.project

    override val priority: WebSymbol.Priority?
      get() = delegate.priority

    override val attributeValue: WebSymbolHtmlAttributeValue?
      get() = delegate.attributeValue

    override val isSignalProperty: Boolean
      get() = delegate.isSignalProperty

    override val virtualProperty: Boolean
      get() = false

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      delegate.getNavigationTargets(project)

    abstract override fun createPointer(): Pointer<out AbstractFromInOutDirectiveProperty>

    final override fun equals(other: Any?): Boolean =
      this === other || other is InputDirectiveProperty
      && delegate == other.delegate

    final override fun hashCode(): Int =
      delegate.hashCode()

    final override fun toString(): String =
      "$delegate as " + qualifiedKind.kind

    override fun isEquivalentTo(symbol: Symbol): Boolean {
      return symbol == this || delegate.isEquivalentTo(symbol)
    }

    final override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
      Angular2ElementDocumentationTarget.create(
        name, location, delegate,
        Angular2EntitiesProvider.getEntity(delegate.sourceElement.contextOfType<TypeScriptClass>(true)))
      ?: super<Angular2DirectiveProperty>.getDocumentationTarget(location)
  }

  private class InOutDirectiveProperty(input: Angular2DirectiveProperty,
                                       private val myOutput: Angular2DirectiveProperty)
    : Angular2SymbolDelegate<Angular2DirectiveProperty>(input) {

    override val qualifiedKind: WebSymbolQualifiedKind
      get() = NG_DIRECTIVE_IN_OUTS


    override val apiStatus: WebSymbolApiStatus
      get() = delegate.apiStatus.coalesceWith(myOutput.apiStatus)

    override fun createPointer(): Pointer<out InOutDirectiveProperty> {
      val input = delegate.createPointer()
      val output = myOutput.createPointer()
      return Pointer {
        val newInput = input.dereference()
        val newOutput = output.dereference()
        if (newInput != null && newOutput != null) InOutDirectiveProperty(newInput, newOutput) else null
      }
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || javaClass != other.javaClass) return false
      val property = other as InOutDirectiveProperty?
      return myOutput == property!!.myOutput && delegate == property.delegate
    }

    override fun hashCode(): Int {
      return Objects.hash(myOutput, delegate)
    }

    override fun toString(): String {
      return "<$delegate,$myOutput>"
    }

    override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
      Angular2ElementDocumentationTarget.create(
        name, location, delegate, myOutput,
        Angular2EntitiesProvider.getEntity(delegate.sourceElement.contextOfType<TypeScriptClass>(true)))
      ?: super.getDocumentationTarget(location)
  }

}
