package com.intellij.coldFusion.patterns

import com.intellij.coldFusion.model.psi.CfmlLeafPsiElement
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.patterns.InitialPatternCondition
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext

/**
 * @author Sergey Karashevich
 */
object CfmlPatternsUtil {

  fun sqlCapture(): LeafPsiElementPattern.Capture<LeafPsiElement> {
    val initPatternCond: InitialPatternCondition<LeafPsiElement> = object : InitialPatternCondition<LeafPsiElement>(
      LeafPsiElement::class.java) {
      override fun accepts(obj: Any?, context: ProcessingContext) =
        (obj is CfmlLeafPsiElement) &&
        (obj.parent != null) &&
        (obj.parent is CfmlTagImpl) &&
        (obj.parent as CfmlTagImpl).name == "cfquery"
    }
    return LeafPsiElementPattern.Capture(initPatternCond)
  }

}