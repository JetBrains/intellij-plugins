package org.angular2.entities

import com.intellij.javascript.webSymbols.apiStatus
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.types.JSExoticStringLiteralType
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbolApiStatus
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder.Companion.BINDING_GUARD

interface Angular2ClassBasedDirective : Angular2Directive, Angular2ClassBasedEntity {

  override val apiStatus: WebSymbolApiStatus
    get() = typeScriptClass?.apiStatus ?: WebSymbolApiStatus.Stable

  override val templateGuards: List<Angular2TemplateGuard>
    get() = typeScriptClass?.members
              ?.asSequence()
              ?.filter { it.name?.startsWith(Angular2ControlFlowBuilder.NG_TEMPLATE_GUARD_PREFIX) == true }
              ?.map { guard ->
                Angular2TemplateGuard(
                  guard.name!!.removePrefix(Angular2ControlFlowBuilder.NG_TEMPLATE_GUARD_PREFIX),
                  if (guard.asSafely<TypeScriptField>()
                      ?.jsType?.let {
                        if (it is JSExoticStringLiteralType)
                          it.asSimpleLiteralType()
                        else
                          it as? JSStringLiteralTypeImpl
                      }?.literal == BINDING_GUARD)
                    Angular2TemplateGuard.Kind.Binding
                  else
                    Angular2TemplateGuard.Kind.Method,
                  guard
                )
              }
              ?.toList()
            ?: emptyList()

  override val hasTemplateContextGuard: Boolean
    get() = typeScriptClass?.members?.any { it.name == Angular2ControlFlowBuilder.NG_TEMPLATE_CONTEXT_GUARD } == true
}