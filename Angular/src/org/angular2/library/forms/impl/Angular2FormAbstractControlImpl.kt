package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.refactoring.WebSymbolRenameTarget
import com.intellij.webSymbols.search.WebSymbolSearchTarget
import org.angular2.Angular2Framework
import org.angular2.library.forms.Angular2FormControl

abstract class Angular2FormAbstractControlImpl(
  override val source: PsiElement,
) : Angular2FormControl {

  override val origin: WebSymbolOrigin
    get() = WebSymbolOrigin.create(Angular2Framework.ID)

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

}