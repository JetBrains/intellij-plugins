package com.intellij.dts.pp.lang.psi

class PpIncludeStatement(statement: PpStatement) : PpStatement by statement {
  val headerName = findFirstOrNullToken(tokenTypes.headerName)
}
