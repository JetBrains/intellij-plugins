package com.intellij.dts.pp.lang.psi

enum class PpStatementType(val directive: String?) {
  Unknown(null),
  Include("include"),
  Define("define"),
  Undef("undef"),
  Warning("warning"),
  Error("error"),
  Pragma("pragma"),
  Line("line"),
  If("if"),
  IfDef("ifdef"),
  IfNdef("ifndef"),
  Else("else"),
  Elif("elif"),
  ElifDef("elifdef"),
  ElifNdef("elifndef"),
  Endif("endif"),
}
