package com.intellij.dts.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

sealed interface DtsValue : PsiElement {
  val dtsValueRange: TextRange
    get() = textRange

  interface Parseable<T> : DtsValue {
    fun dtsParse(): T
  }

  interface Int : Parseable<kotlin.Int?>

  interface Byte : Parseable<kotlin.Int?>

  interface Expression : Int

  interface String : Parseable<kotlin.String>

  interface Char : Parseable<kotlin.Char?>

  interface PHandle : DtsValue {
    val dtsPHandleLabel: PsiElement?

    val dtsPHandlePath: PsiElement?
  }

  interface Macro : DtsValue
}