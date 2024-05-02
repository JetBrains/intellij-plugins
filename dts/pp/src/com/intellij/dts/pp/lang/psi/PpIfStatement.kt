package com.intellij.dts.pp.lang.psi

private fun PpStatement.evaluateIfExpr(defines: List<String>): Boolean {
  // TODO: If expressions are currently not supported
  return true
}

private fun PpStatement.evaluateIfDef(defines: List<String>): Boolean {
  val text = identifier?.text ?: return false
  return defines.any(text::equals)
}

private fun PpStatement.evaluateIfNdef(defines: List<String>): Boolean {
  val text = identifier?.text ?: return true
  return defines.none(text::equals)
}

interface PpIfStatement : PpStatement {
  fun evaluate(defines: List<String>): Boolean
}

class PpIfExprStatement(statement: PpStatement) : PpStatement by statement, PpIfStatement {
  override fun evaluate(defines: List<String>): Boolean = evaluateIfExpr(defines)
}

class PpIfDefStatement(statement: PpStatement) : PpStatement by statement, PpIfStatement {
  override fun evaluate(defines: List<String>): Boolean = evaluateIfDef(defines)
}

class PpIfNdefStatement(statement: PpStatement) : PpStatement by statement, PpIfStatement {
  override fun evaluate(defines: List<String>): Boolean = evaluateIfNdef(defines)
}

interface PpElifStatement : PpStatement {
  fun evaluate(defines: List<String>): Boolean
}

class PpElseStatement(statement: PpStatement) : PpStatement by statement, PpElifStatement {
  override fun evaluate(defines: List<String>): Boolean = true
}

class PpElifExprStatement(statement: PpStatement) : PpStatement by statement, PpElifStatement {
  override fun evaluate(defines: List<String>): Boolean = evaluateIfExpr(defines)
}

class PpElifDefStatement(statement: PpStatement) : PpStatement by statement, PpElifStatement {
  override fun evaluate(defines: List<String>): Boolean = evaluateIfDef(defines)
}

class PpElifNdefStatement(statement: PpStatement) : PpStatement by statement, PpElifStatement {
  override fun evaluate(defines: List<String>): Boolean = evaluateIfNdef(defines)
}
