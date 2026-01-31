package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsByte
import com.intellij.dts.lang.psi.DtsChar
import com.intellij.dts.lang.psi.DtsExprValue
import com.intellij.dts.lang.psi.DtsInt
import com.intellij.dts.lang.psi.DtsString
import com.intellij.dts.lang.resolve.DtsBindingReference
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.util.DtsUtil
import com.intellij.dts.util.trim
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange

abstract class DtsIntMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsInt {
  override fun dtsParse(): Int? {
    val stripedText = text.replace("U", "").replace("L", "")

    return try {
      Integer.decode(stripedText)
    }
    catch (e: NumberFormatException) {
      null
    }
  }
}

abstract class DtsByteMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsByte {
  override fun dtsParse(): Int? {
    return try {
      Integer.parseInt(text, 16)
    }
    catch (e: NumberFormatException) {
      null
    }
  }
}

abstract class DtsExpressionMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsExprValue {
  override fun dtsParse(): Int? {
    // expression evaluation not implemented
    return null
  }
}

abstract class DtsStringMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsString {
  override val dtsValueRange: TextRange
    get() = textRange.trim(text, '"')

  override fun dtsParse(): String = text.trim('"')

  override fun getOwnReferences(): Collection<PsiSymbolReference> {
    return DtsUtil.singleResult {
      val property = DtsTreeUtil.parentProperty(this) ?: return@singleResult null
      if (property.dtsName != "compatible") return@singleResult null

      DtsBindingReference(this)
    }
  }
}

abstract class DtsCharMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsChar {
  override val dtsValueRange: TextRange
    get() = textRange.trim(text, '\'')

  override fun dtsParse(): Char? = text.trim('\'').elementAtOrNull(0)
}
