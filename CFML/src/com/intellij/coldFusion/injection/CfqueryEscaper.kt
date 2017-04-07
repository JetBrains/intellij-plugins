package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper

class CfqueryEscaper(tag: CfmlTagImpl) : LiteralTextEscaper<CfmlTagImpl>(tag) {

  override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
    var subText = rangeInsideHost.substring(myHost.text)
    myHost.children.forEach {
      if (it is CfmlTagImpl)
        when (it.name.toLowerCase()) {
          "cfqueryparam" -> subText = subText.replaceFirst(it.text, createIdentifierDummy(it.text), true)
          "cfif" ->
            if (it.hasClosedTag())
              subText = subText.replaceFirst(it.text, createIdentifierDummy(it.text), true)
        }
    }
    outChars.append(subText)
    return true
  }

  override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange) = offsetInDecoded + rangeInsideHost.startOffset

  override fun isOneLine() = false

  //exclude cfquery tags from TextRange
  override fun getRelevantTextRange(): TextRange {
    val startOffset = Regex("<cfquery[^>]*>").find(myHost.text)!!.range.endInclusive + 1
    val endOffset = Regex("</cfquery[^>]*>").find(myHost.text)!!.range.start

    return TextRange(startOffset, endOffset)
  }

  /////// Util methods

  private fun CfmlTagImpl.hasClosedTag(): Boolean = Regex("</${this.tagName}[^>]*>").containsMatchIn(this.text)

  private fun createIdentifierDummy(text: String): String {
    val sb = StringBuilder().append("'<CFD")
    (0..text.length - 8).forEach { sb.append("-") }
    sb.append(">'")
    return sb.toString()
  }
}