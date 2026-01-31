package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsByteArray
import com.intellij.dts.lang.psi.DtsCellArray
import com.intellij.dts.lang.psi.DtsCellArrayBits
import com.intellij.dts.lang.psi.DtsInt
import com.intellij.dts.lang.psi.DtsValue
import com.intellij.dts.util.trim
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange

abstract class DtsCellArrayMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsCellArray {
  override val dtsValueRange: TextRange
    get() = textRange.trim(text,'<', '>')

  override val dtsValues: List<DtsValue>
    get() = findChildrenByClass(DtsValue::class.java).toList()

  override val dtsBits: Int?
    get() {
      // if no bits annotation is present the default value is 32
      val bits = findChildByClass(DtsCellArrayBits::class.java) ?: return 32

      val bitsValue = bits.dtsBitsValue
      return if (bitsValue is DtsInt) {
        bitsValue.dtsParse()
      }
      else {
        // if the value of the bits annotation is not an int the value cannot be determined
        null
      }
    }
}

abstract class DtsByteArrayMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsByteArray {
  override val dtsValueRange: TextRange
    get() = textRange.trim(text,'[', ']')

  override val dtsValues: List<DtsValue>
    get() = findChildrenByClass(DtsValue::class.java).toList()
}