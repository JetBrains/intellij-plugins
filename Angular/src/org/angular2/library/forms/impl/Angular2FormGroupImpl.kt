package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import org.angular2.library.forms.Angular2FormAbstractControl
import org.angular2.library.forms.Angular2FormGroup
import org.angular2.library.forms.NG_FORM_GROUP_FIELDS
import org.angular2.library.forms.NG_FORM_GROUP_PROPS

class Angular2FormGroupImpl(
  source: PsiElement,
  override val initializer: JSObjectLiteralExpression?,
  override val members: List<Angular2FormAbstractControl>,
) : Angular2FormGroup, Angular2FormAbstractControlImpl(source) {

  init {
    assert(source is TypeScriptField || source is JSProperty)
  }

  override fun getSymbols(
    qualifiedKind: WebSymbolQualifiedKind,
    params: WebSymbolsListSymbolsQueryParams,
    scope: Stack<WebSymbolsScope>,
  ): List<WebSymbolsScope> =
    members.filter { it.qualifiedKind == qualifiedKind }

  override val qualifiedKind: WebSymbolQualifiedKind
    get() = if (source is TypeScriptField) NG_FORM_GROUP_FIELDS else NG_FORM_GROUP_PROPS

  override fun createPointer(): Pointer<Angular2FormGroupImpl> {
    val sourcePtr = source.createSmartPointer()
    val initializerPtr = initializer?.createSmartPointer()
    val membersPtr = members.map { it.createPointer() }
    return Pointer {
      val members = membersPtr.map { it.dereference() ?: return@Pointer null }
      val initializer = initializerPtr?.let { it.element ?: return@Pointer null }
      sourcePtr.element?.let { Angular2FormGroupImpl(it, initializer, members) }
    }
  }
}