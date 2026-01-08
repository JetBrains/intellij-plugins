package org.angular2.web.scopes

import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.js.JS_STRING_LITERALS
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.AstLoadingFilter
import com.intellij.util.asSafely
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.index.getFunctionNameFromIndex
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.NG_DIRECTIVE_IN_OUTS
import org.angular2.web.NG_DIRECTIVE_OUTPUTS

const val CREATE_COMPONENT_FUN: String = "createComponent"
const val INPUT_BINDING_FUN: String = "inputBinding"
const val OUTPUT_BINDING_FUN: String = "outputBinding"
const val TWO_WAY_BINDING_FUN: String = "twoWayBinding"
const val BINDINGS_PROP: String = "bindings"
const val TYPE_PROP: String = "type"

class CreateComponentDirectiveBindingScope(objectLiteral: JSObjectLiteralExpression) :
  PolySymbolScopeWithCache<JSObjectLiteralExpression, Unit>(objectLiteral.project, objectLiteral, Unit) {

  companion object {
    val INPUTS_SCOPE: PolySymbolScope = PolySymbolReferencingScope(JS_STRING_LITERALS, "Angular directive input",
                                                                   true, NG_DIRECTIVE_INPUTS)
    val OUTPUTS_SCOPE: PolySymbolScope = PolySymbolReferencingScope(JS_STRING_LITERALS, "Angular directive output",
                                                                    true, NG_DIRECTIVE_OUTPUTS)
    val IN_OUTS_SCOPE: PolySymbolScope = PolySymbolReferencingScope(JS_STRING_LITERALS, "Angular directive two-way binding",
                                                                    true, NG_DIRECTIVE_IN_OUTS)
  }

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    val jsType =
      dataHolder.findProperty(TYPE_PROP)
        ?.jsType
      ?: dataHolder.context
        ?.let { if (it is JSArgumentList) it.context else it }
        ?.asSafely<JSCallExpression>()
        ?.takeIf { getFunctionNameFromIndex(it) == CREATE_COMPONENT_FUN }
        ?.let {
          AstLoadingFilter.forceAllowTreeLoading<Array<JSExpression>, Throwable>(it.containingFile) {
            it.arguments
          }
        }
        ?.getOrNull(0)
        ?.let { JSResolveUtil.getElementJSType(it) }

    val cls = jsType?.substitute()?.sourceElement as? TypeScriptClass
    val directive = Angular2EntitiesProvider.getDirective(cls)
                    ?: return
    directive.inputs.forEach(consumer)
    directive.outputs.forEach(consumer)
    directive.inOuts.forEach(consumer)
  }

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == NG_DIRECTIVE_INPUTS
    || kind == NG_DIRECTIVE_OUTPUTS
    || kind == NG_DIRECTIVE_IN_OUTS

  override fun createPointer(): Pointer<CreateComponentDirectiveBindingScope> {
    val objectLiteralPtr = dataHolder.createSmartPointer()
    return Pointer {
      objectLiteralPtr.dereference()?.let { CreateComponentDirectiveBindingScope(it) }
    }
  }

}