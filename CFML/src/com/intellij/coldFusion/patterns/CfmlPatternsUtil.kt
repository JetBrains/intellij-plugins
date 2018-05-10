package com.intellij.coldFusion.patterns

import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.patterns.InitialPatternCondition
import com.intellij.util.ProcessingContext

/**
 * @author Sergey Karashevich
 */
object CfmlPatternsUtil {

  fun sqlCapture(): CfmlTagImplPattern.Capture<CfmlTagImpl> {
    return CfmlTagImplPattern.Capture(object : InitialPatternCondition<CfmlTagImpl>(
      CfmlTagImpl::class.java) {

      override fun accepts(cfmlTag: Any?, context: ProcessingContext): Boolean =
        listOf(::cfQueryHost, ::cfIfElseHost, ::cfElseHost).any { it(cfmlTag) }
    })
  }

  private fun cfQueryHost(cfmlTag: Any?): Boolean {
    if (!(cfmlTag is CfmlTagImpl && cfmlTag.name?.toLowerCase() == "cfquery")) return false
    return cfmlTag.text.contains(Regex("<cfquery[^>]*>"))
  }

  private fun cfElseHost(cfmlTag: Any?): Boolean {
    if (!(cfmlTag is CfmlTagImpl && cfmlTag.name?.toLowerCase() == "cfelse")) return false
    return cfQueryHost(cfmlTag.parent?.parent)
  }

  private fun cfIfElseHost(cfmlTag: Any?): Boolean {
    if (!(cfmlTag is CfmlTagImpl && cfmlTag.name?.toLowerCase() == "cfifelse")) return false
    return cfQueryHost(cfmlTag.parent?.parent)
  }


}