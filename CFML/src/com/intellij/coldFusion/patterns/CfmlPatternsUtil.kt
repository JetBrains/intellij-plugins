package com.intellij.coldFusion.patterns

import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.patterns.InitialPatternCondition
import com.intellij.util.ProcessingContext

/**
 * @author Sergey Karashevich
 */
object CfmlPatternsUtil {

  fun sqlCapture(): CfmlTagImplPattern.Capture<CfmlTagImpl> {
    val initPatternCond: InitialPatternCondition<CfmlTagImpl> = object : InitialPatternCondition<CfmlTagImpl>(CfmlTagImpl::class.java) {
      override fun accepts(obj: Any?, context: ProcessingContext): Boolean {
        val isCfquery = (obj is CfmlTagImpl && obj.name == "cfquery")
        if (!isCfquery) return false
        val cfmlTag = obj as CfmlTagImpl
        return cfmlTag.text.contains(Regex("<cfquery[^>]*>"))
      }
    }
    return CfmlTagImplPattern.Capture(initPatternCond)
  }

}