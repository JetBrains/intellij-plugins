package org.angular2

import com.intellij.openapi.util.text.StringUtil

fun templateBindingVarToDirectiveInput(name: String, templateName: String): String =
  templateName + StringUtil.capitalize(name)

fun directiveInputToTemplateBindingVar(name: String, templateName: String): String =
  StringUtil.decapitalize(name.removePrefix(templateName))

fun isTemplateBindingDirectiveInput(name: String, templateName: String): Boolean =
  name.startsWith(templateName) && name.getOrNull(templateName.length)?.isUpperCase() == true