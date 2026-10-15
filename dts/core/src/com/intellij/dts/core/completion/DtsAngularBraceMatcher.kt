package com.intellij.dts.core.completion

import com.intellij.dts.core.lang.DtsLanguage
import com.intellij.dts.core.lang.psi.DtsTypes
import com.intellij.dts.pp.completion.PpAngularBraceBackspaceHandler
import com.intellij.dts.pp.completion.PpAngularBraceTypedHandler
import com.intellij.lang.Language
import com.intellij.psi.tree.IElementType

private val bracePairs = listOf(DtsTypes.LANGL to DtsTypes.RANGL)

internal class DtsAngularBraceTypedHandler : PpAngularBraceTypedHandler() {
  override fun getLanguage(): Language = DtsLanguage

  override fun getPairs(): List<Pair<IElementType, IElementType>> = bracePairs
}

internal class DtsAngularBraceBackspaceHandler : PpAngularBraceBackspaceHandler() {
  override fun getLanguage(): Language = DtsLanguage

  override fun getPairs(): List<Pair<IElementType, IElementType>> = bracePairs
}