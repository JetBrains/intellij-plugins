package org.angular2

import com.intellij.openapi.util.text.StringUtil
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.PropertyBindingType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun templateBindingVarToDirectiveInput(name: String, templateName: String): String =
  templateName + StringUtil.capitalize(name)

fun directiveInputToTemplateBindingVar(name: String, templateName: String): String =
  StringUtil.decapitalize(name.removePrefix(templateName))

fun isTemplateBindingDirectiveInput(name: String, templateName: String): Boolean =
  name.startsWith(templateName) && name.getOrNull(templateName.length)?.isUpperCase() == true

@OptIn(ExperimentalContracts::class)
fun isCustomCssPropertyBinding(info: Angular2AttributeNameParser.AttributeInfo): Boolean {
  contract {
    returns(true) implies (info is Angular2AttributeNameParser.PropertyBindingInfo)
  }
  return (info is Angular2AttributeNameParser.PropertyBindingInfo
          && info.bindingType == PropertyBindingType.STYLE
          && info.name.startsWith("--")
          && info.name.length > 2)
}