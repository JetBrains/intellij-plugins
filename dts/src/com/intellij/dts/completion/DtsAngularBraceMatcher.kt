package com.intellij.dts.completion

import com.intellij.dts.lang.DtsLanguage
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.pp.completion.PpAngularBraceBackspaceHandler
import com.intellij.dts.pp.completion.PpAngularBraceTypedHandler
import com.intellij.lang.Language
import com.intellij.psi.tree.IElementType

private val bracePairs = listOf(DtsTypes.LANGL to DtsTypes.RANGL)

private class DtsAngularBraceTypedHandler : PpAngularBraceTypedHandler() {
  override fun getLanguage(): Language = DtsLanguage

  override fun getPairs(): List<Pair<IElementType, IElementType>> = bracePairs
}

private class DtsAngularBraceBackspaceHandler : PpAngularBraceBackspaceHandler() {
  override fun getLanguage(): Language = DtsLanguage

  override fun getPairs(): List<Pair<IElementType, IElementType>> = bracePairs
}