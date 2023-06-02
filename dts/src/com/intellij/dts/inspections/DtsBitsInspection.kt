package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.lang.psi.DtsCellArrayBits
import com.intellij.dts.lang.psi.DtsInt
import com.intellij.dts.lang.psi.dtsVisitor

class DtsBitsInspection : LocalInspectionTool() {
    companion object {
        private val validBits = arrayOf(8, 16, 32, 64)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = dtsVisitor(DtsCellArrayBits::class) {
        checkBits(it, holder)
    }

    private fun checkBits(bits: DtsCellArrayBits, holder: ProblemsHolder) {
        val bitsValue = bits.dtsBitsValue
        if (bitsValue !is DtsInt) return

        val parsed = bitsValue.dtsParse()
        if (parsed == null || parsed in validBits) return

        holder.registerError(bitsValue, bundleKey = "inspections.bits.bad_size")
    }
}