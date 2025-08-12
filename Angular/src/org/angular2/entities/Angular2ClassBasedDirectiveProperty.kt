package org.angular2.entities

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2EntityUtils.NG_ACCEPT_INPUT_TYPE_PREFIX
import org.angular2.web.NG_DIRECTIVE_INPUTS

interface Angular2ClassBasedDirectiveProperty : Angular2DirectiveProperty {

  val owner: TypeScriptClass?

  val objectInitializer: JSObjectLiteralExpression?
    get() = null

  override val isCoerced: Boolean
    get() = fieldName?.let { fieldName ->
      owner
        ?.staticJSType
        ?.asRecordType(owner)
        ?.findPropertySignature(NG_ACCEPT_INPUT_TYPE_PREFIX + fieldName)
    } != null
            || objectInitializer?.findProperty(Angular2DecoratorUtil.TRANSFORM_PROP) != null

  override val type: JSType?
    get() = if (qualifiedKind == NG_DIRECTIVE_INPUTS)
      fieldName?.let { Angular2EntityUtils.jsTypeFromAcceptInputType(owner, it) } ?: super.type
    else
      super.type

  override val transformParameterType: JSType?
    get() = objectInitializer?.findProperty(Angular2DecoratorUtil.TRANSFORM_PROP)
      ?.jsType
      ?.asRecordType(owner)
      ?.callSignatures
      ?.firstNotNullOfOrNull { signature -> signature.functionType.parameters.takeIf { it.isNotEmpty() }?.get(0) }
      ?.inferredType
}