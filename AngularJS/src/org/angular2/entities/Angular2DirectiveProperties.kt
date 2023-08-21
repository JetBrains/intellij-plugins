// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.utils.coalesceWith
import org.angular2.codeInsight.documentation.Angular2ElementDocumentationTarget
import org.angular2.lang.Angular2LangUtil.OUTPUT_CHANGE_SUFFIX
import org.angular2.web.Angular2Symbol
import org.angular2.web.Angular2SymbolDelegate
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_IN_OUTS
import java.util.*

class Angular2DirectiveProperties(inputs: Collection<Angular2DirectiveProperty>,
                                  outputs: Collection<Angular2DirectiveProperty>) {

  val inputs: Collection<Angular2DirectiveProperty> = Collections.unmodifiableCollection(inputs)
  val outputs: Collection<Angular2DirectiveProperty> = Collections.unmodifiableCollection(outputs)

  private val myInOuts = NotNullLazyValue.createValue<List<Angular2Symbol>> {
    if (inputs.isEmpty() || outputs.isEmpty()) {
      return@createValue emptyList<Angular2Symbol>()
    }
    val inputMap = HashMap<String, Angular2DirectiveProperty>()
    for (p in inputs) {
      inputMap.putIfAbsent(p.name, p)
    }
    val result = ArrayList<Angular2Symbol>()
    for (output in outputs) {
      val name = output.name
      if (output.name.endsWith(OUTPUT_CHANGE_SUFFIX)) {
        val input = inputMap[name.substring(0, name.length - OUTPUT_CHANGE_SUFFIX.length)]
        if (input != null) {
          result.add(InOutDirectiveProperty(input, output))
        }
      }
    }
    result
  }

  val inOuts: List<Angular2Symbol>
    get() = myInOuts.value

  private class InOutDirectiveProperty(input: Angular2DirectiveProperty, private val myOutput: Angular2DirectiveProperty)
    : Angular2SymbolDelegate<Angular2DirectiveProperty>(input) {

    override val namespace: String
      get() = WebSymbol.NAMESPACE_JS

    override val kind: String
      get() = KIND_NG_DIRECTIVE_IN_OUTS


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
