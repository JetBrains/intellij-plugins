package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.coldFusion.patterns.CfmlPatterns
import com.intellij.psi.PsiLanguageInjectionHost
import org.intellij.plugins.intelliLang.inject.AbstractLanguageInjectionSupport

/**
 * @author Sergey Karashevich
 */
class CfmlLanguageInjectionSupport : AbstractLanguageInjectionSupport() {

  override fun getId() = SUPPORT_ID

  override fun getPatternClasses() = arrayOf(CfmlPatterns::class.java)

  override fun isApplicableTo(host: PsiLanguageInjectionHost) = (host is CfmlTagImpl && host.name != null && host.name.toLowerCase() == "cfquery")

  override fun useDefaultInjector(host: PsiLanguageInjectionHost): Boolean = false

  override fun getHelpId(): String? = null

  companion object {
    val SUPPORT_ID = "cfml"
  }
}
