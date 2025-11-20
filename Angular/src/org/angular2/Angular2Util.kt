package org.angular2

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6NamedImports
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.resolve.JSResolveProcessorBase
import com.intellij.lang.javascript.psi.util.JSFindFirstResolveProcessor
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil.processDeclarationsInScope
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.util.asSafely
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

fun isFromImportedPackage(name: String, context: PsiElement, vararg packageNames: String): Boolean =
  FindFirstFieldIgnoringResolveProcessor(name)
    .also {
      processDeclarationsInScope(context, it, true)
    }
    .result
    ?.asSafely<ES6ImportSpecifier>()
    ?.context.asSafely<ES6NamedImports>()
    ?.context.asSafely<ES6ImportDeclaration>()
    ?.fromClause
    ?.referenceText
    ?.let { JSStringUtil.unquoteAndUnescapeString(it) }
    ?.let { importName -> packageNames.any { it == importName } } == true

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

private class FindFirstFieldIgnoringResolveProcessor(name: String) : JSResolveProcessorBase(name) {
  var result: PsiElement? = null

  override fun execute(element: PsiElement, state: ResolveState): Boolean {
    if (element !is TypeScriptField && JSFindFirstResolveProcessor.checkName(element, myName)) {
      result = element
      return false
    }
    return true
  }
}