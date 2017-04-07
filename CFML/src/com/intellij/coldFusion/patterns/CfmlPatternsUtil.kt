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
      override fun accepts(obj: Any?, context: ProcessingContext) = (obj is CfmlTagImpl && obj.name == "cfquery")
    }
    return CfmlTagImplPattern.Capture(initPatternCond)
  }

}