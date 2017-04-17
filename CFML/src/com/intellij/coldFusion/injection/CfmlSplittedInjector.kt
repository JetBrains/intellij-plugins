package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.psi.CfmlLeafPsiElement
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.Trinity
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.psi.impl.source.tree.injected.MultiHostRegistrarImpl
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.plugins.intelliLang.Configuration
import org.intellij.plugins.intelliLang.inject.InjectedLanguage
import org.intellij.plugins.intelliLang.inject.InjectorUtils
import org.intellij.plugins.intelliLang.inject.LanguageInjectionSupport
import org.intellij.plugins.intelliLang.inject.TemporaryPlacesRegistry


/**
 * @author Sergey Karashevich
 */
class CfmlSplittedInjector(val myConfiguration: Configuration,
                           val myProject: Project,
                           val myTemporaryPlacesRegistry: TemporaryPlacesRegistry) : SplittedInjector {

  private val mySupport: LanguageInjectionSupport = InjectorUtils.findNotNullInjectionSupport(CfmlLanguageInjectionSupport.SUPPORT_ID)


  override fun getLanguagesToInject(registrar: MultiHostRegistrar, vararg splittedElements: PsiElement) {
    val list = collectSplittedInjections(PsiTreeUtil.findCommonParent(*splittedElements)!!, *splittedElements)
    if (list.isEmpty()) return
    val lang = list[0].second.language
    processInjection(lang!!, list, splittedElements[0].containingFile, registrar)
  }

  fun collectSplittedInjections(head: PsiElement,
                                vararg splittedElements: PsiElement): List<Trinity<PsiLanguageInjectionHost, InjectedLanguage, TextRange>> {
    val myInjectionsList: MutableList<Trinity<PsiLanguageInjectionHost, InjectedLanguage, TextRange>> = mutableListOf()
    for (injection in myConfiguration.getInjections(mySupport.id)) {
      if (injection.acceptsPsiElement(head)) {
        splittedElements
          .filter { it is PsiLanguageInjectionHost }
          .forEach { element ->
            myInjectionsList.add(Trinity(element as PsiLanguageInjectionHost,
                                         InjectedLanguage.create(injection.injectedLanguageId)!!, TextRange(0, element.text.length)))
          }
      }
    }
    return myInjectionsList
  }

  fun processInjection(lang: Language,
                       injectionList: List<Trinity<PsiLanguageInjectionHost, InjectedLanguage, TextRange>>,
                       finalContainingFile: PsiFile,
                       registrar: MultiHostRegistrar) {
    registerInjection(lang, injectionList, finalContainingFile, registrar)
    InjectorUtils.registerSupport(mySupport, false, registrar)
    InjectorUtils.putInjectedFileUserData(registrar, InjectedLanguageUtil.FRANKENSTEIN_INJECTION, null)
  }

  //we are overriding this method from InjectorUtils.registerInjection(...) to replace suffixes with our replacement
  fun registerInjection(lang: Language,
                        injectionList: List<Trinity<PsiLanguageInjectionHost, InjectedLanguage, TextRange>>,
                        finalContainingFile: PsiFile,
                        registrar: MultiHostRegistrar) {
    var injectionStarted = false
    for (t in injectionList) {
      val host = t.first
      if (host.containingFile !== finalContainingFile) continue

      val textRange = t.third
      val injectedLanguage = t.second

      if (!injectionStarted) {
        registrar.startInjecting(lang)
        // TextMate language requires file extension
        if (registrar is MultiHostRegistrarImpl && !StringUtil.equalsIgnoreCase(lang.id, t.second.id)) {
          registrar.setFileExtension(StringUtil.toLowerCase(t.second.id))
        }
        injectionStarted = true
      }
      registrar.addPlace(injectedLanguage.prefix, getSuffix(host) , host, textRange)
    }
    if (injectionStarted)
      registrar.doneInjecting()
  }

  //create dummy for <cfqueryparam> and <cfif> CFML tags in SQL injection to resolve SQL expression without errors
  fun getSuffix(host: PsiLanguageInjectionHost): String? {
    if (host is CfmlLeafPsiElement && host.parent is CfmlTagImpl) {
      if (host.nextSibling != null
          && host.nextSibling is CfmlTagImpl) {
        val nextSibling = host.nextSibling as CfmlTagImpl
        when(nextSibling.name.toLowerCase()) {
          "cfqueryparam" -> return "'parameter from <cfqueryparam>'"
          "cfif" -> return "'parameter from <cfif>'"
        }
      }
    }
    return null
  }

}
