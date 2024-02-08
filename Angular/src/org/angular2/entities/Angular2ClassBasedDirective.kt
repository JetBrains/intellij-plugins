package org.angular2.entities

import com.intellij.javascript.webSymbols.apiStatus
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.webSymbols.WebSymbolApiStatus
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder

interface Angular2ClassBasedDirective : Angular2Directive, Angular2ClassBasedEntity {

  override val apiStatus: WebSymbolApiStatus
    get() = typeScriptClass?.apiStatus ?: WebSymbolApiStatus.Stable

  override val templateGuards: List<JSElement>
    get() = typeScriptClass?.members?.filter { it.name?.startsWith(Angular2ControlFlowBuilder.NG_TEMPLATE_GUARD_PREFIX) == true }
            ?: emptyList()
}