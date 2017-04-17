package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.coldFusion.patterns.CfmlPatterns
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.intellij.plugins.intelliLang.inject.AbstractLanguageInjectionSupport
import org.intellij.plugins.intelliLang.inject.config.BaseInjection

/**
 * @author Sergey Karashevich
 */
class CfmlLanguageInjectionSupport : AbstractLanguageInjectionSupport() {

  override fun getId() = SUPPORT_ID

  override fun getPatternClasses() = arrayOf(CfmlPatterns::class.java)

  override fun isApplicableTo(host: PsiLanguageInjectionHost) =  (host is CfmlTagImpl && host.name != null && host.name.toLowerCase() == "cfquery")

  override fun useDefaultInjector(host: PsiLanguageInjectionHost): Boolean = true

  override fun getHelpId(): String? = null

  override fun findCommentInjection(host: PsiElement, commentRef: Ref<PsiElement>?): BaseInjection? = super.findCommentInjection(host, commentRef)

  companion object {
    val SUPPORT_ID = "cfml"
  }

}
