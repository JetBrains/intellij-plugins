package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.refactoring.WebSymbolRenameTarget
import com.intellij.webSymbols.search.WebSymbolSearchTarget
import org.angular2.library.forms.Angular2FormControl
import org.angular2.library.forms.NG_FORM_ANY_CONTROL_PROPS
import org.angular2.library.forms.NG_FORM_GROUP_FIELDS
import org.angular2.web.Angular2SymbolOrigin

abstract class Angular2FormAbstractControlImpl(
  override val source: PsiElement,
) : Angular2FormControl {

  override val origin: WebSymbolOrigin
    get() = Angular2SymbolOrigin.empty

  override val project: Project
    get() = source.project

  override val name: @NlsSafe String
    get() = source.asSafely<TypeScriptField>()?.name
            ?: source.asSafely<JSProperty>()?.name
            ?: "<error>"

  override val searchTarget: WebSymbolSearchTarget?
    get() = WebSymbolSearchTarget.create(this)

  override val renameTarget: WebSymbolRenameTarget?
    get() = WebSymbolRenameTarget.create(this)

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind in NG_FORM_ANY_CONTROL_PROPS
    || qualifiedKind == NG_FORM_GROUP_FIELDS

}