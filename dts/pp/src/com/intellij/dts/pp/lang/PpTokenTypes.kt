package com.intellij.dts.pp.lang

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

interface PpTokenTypes {
  val defineStatement: IElementType
  val defineDirective: IElementType
  val defineValue: IElementType

  val endifStatement: IElementType
  val endifDirective: IElementType

  val ifStatement: IElementType
  val ifDirective: IElementType

  val ifdefStatement: IElementType
  val ifdefDirective: IElementType

  val ifndefStatement: IElementType
  val ifndefDirective: IElementType

  val elifStatement: IElementType
  val elifDirective: IElementType

  val elifdefStatement: IElementType
  val elifdefDirective: IElementType

  val elifndefStatement: IElementType
  val elifndefDirective: IElementType

  val elseStatement: IElementType
  val elseDirective: IElementType

  val includeStatement: IElementType
  val includeDirective: IElementType
  val includePath: IElementType

  val undefStatement: IElementType
  val undefDirective: IElementType

  val symbol: IElementType

  // placeholder for expressions
  val expression: IElementType

  val statementEnd: IElementType
  val statementMarker: IElementType

  fun createScopeSet(): TokenSet

  fun createDirectivesSet(): TokenSet = TokenSet.create(
    includeDirective,
    defineDirective,
    endifDirective,
    ifdefDirective,
    ifndefDirective,
    undefDirective,
    ifDirective,
    elifDirective,
    elifdefDirective,
    elifndefDirective,
    elseDirective,
  )

  fun createStatementSet(): TokenSet = TokenSet.create(
    includeStatement,
    defineStatement,
    endifStatement,
    ifdefStatement,
    ifndefStatement,
    undefStatement,
    ifStatement,
    elifStatement,
    elifdefStatement,
    elifndefStatement,
    elseStatement
  )
}