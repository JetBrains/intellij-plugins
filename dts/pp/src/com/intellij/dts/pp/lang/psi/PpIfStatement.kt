package com.intellij.dts.pp.lang.psi

interface PpIfStatement : PpStatement {
  fun evaluate(defines: List<String>): Boolean
}

class PpIfDefStatement(statement: PpStatement) : PpStatement by statement, PpIfStatement {
  override fun evaluate(defines: List<String>): Boolean {
    val text = identifier?.text ?: return false
    return defines.any(text::equals)}
}

class PpIfNdefStatement(statement: PpStatement) : PpStatement by statement, PpIfStatement {
  override fun evaluate(defines: List<String>): Boolean {
    val text = identifier?.text ?: return true
    return defines.none(text::equals)
  }
}
