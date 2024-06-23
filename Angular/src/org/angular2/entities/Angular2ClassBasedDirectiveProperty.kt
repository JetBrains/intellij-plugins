package org.angular2.entities

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import org.angular2.entities.Angular2EntityUtils.NG_ACCEPT_INPUT_TYPE_PREFIX
import org.angular2.web.NG_DIRECTIVE_INPUTS

interface Angular2ClassBasedDirectiveProperty : Angular2DirectiveProperty {

  val owner: TypeScriptClass?

  val isCoerced: Boolean
    get() = fieldName?.let { fieldName ->
      owner
        ?.staticJSType
        ?.asRecordType(owner)
        ?.findPropertySignature(NG_ACCEPT_INPUT_TYPE_PREFIX + fieldName)
    } != null

  override val type: JSType?
    get() = if (qualifiedKind == NG_DIRECTIVE_INPUTS)
      fieldName?.let { Angular2EntityUtils.jsTypeFromAcceptInputType(owner, it) } ?: super.type
    else
      super.type
}