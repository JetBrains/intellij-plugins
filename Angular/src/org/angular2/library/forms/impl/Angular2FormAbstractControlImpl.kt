package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.angular2.library.forms.Angular2FormControl
import org.angular2.library.forms.NG_FORM_ANY_CONTROL_PROPS
import org.angular2.library.forms.NG_FORM_GROUP_FIELDS
import org.angular2.web.Angular2SymbolOrigin

abstract class Angular2FormAbstractControlImpl(
  override val source: PsiElement,
) : Angular2FormControl {

  override val origin: PolySymbolOrigin
    get() = Angular2SymbolOrigin.empty

  override val project: Project
    get() = source.project

  override val name: @NlsSafe String
    get() = source.asSafely<TypeScriptField>()?.name
            ?: source.asSafely<JSProperty>()?.name
            ?: "<error>"

  override val searchTarget: PolySymbolSearchTarget?
    get() = PolySymbolSearchTarget.create(this)

  override val renameTarget: PolySymbolRenameTarget?
    get() = PolySymbolRenameTarget.create(this)

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    kind in NG_FORM_ANY_CONTROL_PROPS
    || kind == NG_FORM_GROUP_FIELDS

}