package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper

class CfqueryEscaper(tag: CfmlTagImpl) : LiteralTextEscaper<CfmlTagImpl>(tag) {

  override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
    val subText = rangeInsideHost.substring(myHost.text)
    outChars.append(subText)
    return true
  }

  override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int = offsetInDecoded + rangeInsideHost.startOffset

  override fun isOneLine(): Boolean = false

  //exclude cfquery tags from TextRange
  override fun getRelevantTextRange(): TextRange {
    try {
      val startOffset = Regex("<cfquery[^>]*>").find(myHost.text)!!.range.endInclusive + 1
      val closeTagOffset = Regex("</cfquery[^>]*>").find(myHost.text) ?: return TextRange(startOffset, startOffset)
      val endOffset = closeTagOffset.range.start
      return TextRange(startOffset, endOffset)
    } catch (e: KotlinNullPointerException) {
      return TextRange(0, 0)
    }
  }

}