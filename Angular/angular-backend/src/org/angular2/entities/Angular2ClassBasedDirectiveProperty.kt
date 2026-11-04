package org.angular2.entities

import com.intellij.javascript.JSTypeEngineEvaluation.forceTSKotlinTypeEngine
import com.intellij.javascript.typeEngine.JSTypeEngineMode
import com.intellij.javascript.typeEngine.getJSType
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import org.angular2.Angular2DecoratorUtil
import org.angular2.web.NG_DIRECTIVE_INPUTS

interface Angular2ClassBasedDirectiveProperty : Angular2DirectiveProperty {

  val owner: TypeScriptClass?

  val objectInitializer: JSObjectLiteralExpression?
    get() = null

  override val isCoerced: Boolean
    get() = fieldName?.let { Angular2EntityUtils.hasAcceptInputType(owner, it) } == true
            || objectInitializer?.findProperty(Angular2DecoratorUtil.TRANSFORM_PROP) != null

  override val type: JSType?
    get() = if (kind == NG_DIRECTIVE_INPUTS)
      fieldName?.let { Angular2EntityUtils.jsTypeFromAcceptInputType(owner, it) } ?: super.type
    else
      super.type

  /** The template transpiler uses this type, so it comes from a program without Kolar. */
  override val transformParameterType: JSType?
    get() = forceTSKotlinTypeEngine(disableKolar = true) {
      objectInitializer?.findProperty(Angular2DecoratorUtil.TRANSFORM_PROP)
        ?.getJSType(JSTypeEngineMode.Service)
        ?.asRecordType(owner)
        ?.callSignatures
        ?.firstNotNullOfOrNull { signature -> signature.functionType.parameters.takeIf { it.isNotEmpty() }?.get(0) }
        ?.inferredType
    }
}