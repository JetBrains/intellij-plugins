package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement

sealed interface DtsNode : DtsStatement.Node {
  interface Root : DtsNode {
    val dtsSlash: PsiElement
  }

  interface Ref : DtsNode {
    val dtsHandle: DtsPHandle

    val dtsLabels: List<String>
  }

  interface Sub : DtsNode {
    val dtsName: String

    val dtsNameElement: PsiElement

    val dtsLabels: List<String>
  }
}