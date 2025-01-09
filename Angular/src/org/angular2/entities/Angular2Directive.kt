// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.model.Pointer
import com.intellij.webSymbols.WebSymbolApiStatus
import org.angular2.web.Angular2Symbol

interface Angular2Directive : Angular2Declaration {

  val selector: Angular2DirectiveSelector

  val exportAs: Map<String, Angular2DirectiveExportAs>

  val inputs: Collection<Angular2DirectiveProperty>
    get() = bindings.inputs + hostDirectives.flatMap { it.inputs }

  val outputs: Collection<Angular2DirectiveProperty>
    get() = bindings.outputs + hostDirectives.flatMap { it.outputs }

  val inOuts: Collection<Angular2Symbol>
    get() = bindings.inOuts + hostDirectives.flatMap { it.inOuts }

  val bindings: Angular2DirectiveProperties

  val attributes: Collection<Angular2DirectiveAttribute>

  val directiveKind: Angular2DirectiveKind

  val hostDirectives: Collection<Angular2HostDirective>

  val isComponent: Boolean
    get() = false

  val apiStatus: WebSymbolApiStatus

  val templateGuards: List<Angular2TemplateGuard>
    get() = emptyList()

  val hasTemplateContextGuard: Boolean
    get() = false

  fun areHostDirectivesFullyResolved(): Boolean

  override fun createPointer(): Pointer<out Angular2Directive>

  companion object {
    val EMPTY_ARRAY: Array<Angular2Directive?> = arrayOfNulls<Angular2Directive>(0)
  }
}
