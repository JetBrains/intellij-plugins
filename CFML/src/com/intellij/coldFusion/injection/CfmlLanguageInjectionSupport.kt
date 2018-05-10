package com.intellij.coldFusion.injection

import com.intellij.coldFusion.patterns.CfmlPatterns
import com.intellij.psi.PsiLanguageInjectionHost
import org.intellij.plugins.intelliLang.inject.AbstractLanguageInjectionSupport

/**
 * @author Sergey Karashevich
 */
class CfmlLanguageInjectionSupport : AbstractLanguageInjectionSupport() {

  override fun getId() = SUPPORT_ID

  override fun getPatternClasses() = arrayOf(CfmlPatterns::class.java)

  override fun isApplicableTo(host: PsiLanguageInjectionHost) = with(host) { isCfQueryTag() || isCfElseTagInsideCfQuery() || isCfIfElseTagInsideCfQuery() }

  override fun useDefaultInjector(host: PsiLanguageInjectionHost): Boolean = false

  override fun getHelpId(): String? = null

  companion object {
    const val SUPPORT_ID = "cfml"
  }
}
