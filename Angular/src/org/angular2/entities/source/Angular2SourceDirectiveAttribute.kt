// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.javascript.webSymbols.apiStatus
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSDocOwner
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.types.JSBooleanLiteralTypeImpl
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.model.Pointer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.utils.PolySymbolDeclaredInPsi
import org.angular2.Angular2DecoratorUtil.OPTIONAL_PROP
import org.angular2.entities.Angular2DirectiveAttribute
import org.angular2.entities.Angular2EntityUtils
import java.util.*

class Angular2SourceDirectiveAttribute private constructor(
  private val typeSource: JSElement,
  private val literal: JSLiteralExpression,
  override val name: String,
) : Angular2DirectiveAttribute, PolySymbolDeclaredInPsi {

  companion object {
    fun create(typeSource: JSElement, literal: JSLiteralExpression): Angular2SourceDirectiveAttribute? {
      assert(typeSource is JSTypeOwner)
      val name = literal.stubSafeStringValue ?: return null
      return Angular2SourceDirectiveAttribute(typeSource, literal, name)
    }
  }

  override val required: Boolean?
    get() = (typeSource as? TypeScriptField)?.initializerOrStub
      ?.asSafely<JSCallExpression>()
      ?.stubSafeCallArguments
      ?.let {
        if (it.size != 2) return@let true
        it[1].asSafely<JSObjectLiteralExpression>()
          ?.findProperty(OPTIONAL_PROP)
          ?.jsType
          ?.substitute()
          ?.asSafely<JSBooleanLiteralTypeImpl>()
          ?.literal != true
      }

  override val type: JSType?
    get() = (typeSource as JSTypeOwner).getJSType(literal)

  override val sourceElement: PsiElement
    get() = literal

  override val textRangeInSourceElement: TextRange?
    get() = TextRange(1, literal.textRange.length - 1)

  override val navigableElement: PsiElement
    get() = literal.navigationElement

  override val apiStatus: WebSymbolApiStatus
    get() = (typeSource as? JSDocOwner)?.apiStatus
            ?: WebSymbolApiStatus.Stable

  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val attribute = other as Angular2SourceDirectiveAttribute?
    return typeSource == attribute!!.typeSource && name == attribute.name
  }

  override fun hashCode(): Int {
    return Objects.hash(typeSource, name)
  }

  override fun createPointer(): Pointer<Angular2SourceDirectiveAttribute> {
    val literalPtr = literal.createSmartPointer()
    val typeSourcePtr = typeSource.createSmartPointer()
    return Pointer {
      val literal = literalPtr.dereference() ?: return@Pointer null
      val typeSource = typeSourcePtr.dereference() ?: return@Pointer null
      create(typeSource, literal)
    }
  }
}
