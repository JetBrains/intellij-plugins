package org.angular2.entities

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import org.angular2.web.NG_DIRECTIVE_INPUTS

interface Angular2ClassBasedDirectiveProperty : Angular2DirectiveProperty {

  val owner: TypeScriptClass?

  override val type: JSType?
    get() = if (qualifiedKind == NG_DIRECTIVE_INPUTS)
      fieldName?.let { Angular2EntityUtils.jsTypeFromAcceptInputType(owner, it) } ?: super.type
    else
      super.type
}