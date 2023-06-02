package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsCellArray
import com.intellij.dts.lang.psi.DtsCellArrayBits
import com.intellij.dts.lang.psi.DtsInt
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class DtsCellArrayMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsCellArray {
    override val dtsBits: Int?
        get() {
            // if no bits annotation is present the default value is 32
            val bits = findChildByClass(DtsCellArrayBits::class.java) ?: return 32

            val bitsValue = bits.dtsBitsValue
            return if (bitsValue is DtsInt) {
                bitsValue.dtsParse()
            } else {
                // if the value of the bits annotation is not an int the value cannot be determined
                null
            }
        }
}