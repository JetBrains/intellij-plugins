package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes
import com.intellij.coldFusion.model.parsers.CfmlElementTypes
import com.intellij.coldFusion.model.psi.*
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.lang.Language
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.Trinity
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.plugins.intelliLang.Configuration
import org.intellij.plugins.intelliLang.inject.InjectedLanguage
import org.intellij.plugins.intelliLang.inject.InjectorUtils
import org.intellij.plugins.intelliLang.inject.LanguageInjectionSupport

/**
 * @author Sergey Karashevich
 */
class CfmlSplittedInjector(private val myConfiguration: Configuration,
                           val myProject: Project) : SplittedInjector {

  private val mySupport: LanguageInjectionSupport = InjectorUtils.findNotNullInjectionSupport(CfmlLanguageInjectionSupport.SUPPORT_ID)

  override fun processInjection(registrar: MultiHostRegistrar, splittedElements: List<PsiElement>): Boolean {
    val head = getHead(splittedElements.toList()) ?: return false
    val list = collectSplittedInjections(head, splittedElements)
    if (list.isEmpty()) return false
    val lang = list[0].second.language ?: return false
    return processInjection(lang, list, splittedElements[0].containingFile, registrar)
  }

  private fun collectSplittedInjections(head: PsiElement,
                                        splittedElements: List<PsiElement>): List<Trinity<PsiLanguageInjectionHost, InjectedLanguage, TextRange>> {
    val myInjectionsList: MutableList<Trinity<PsiLanguageInjectionHost, InjectedLanguage, TextRange>> = mutableListOf()
    for (injection in myConfiguration.getInjections(mySupport.id)) {
      if (injection.acceptsPsiElement(head)) {
        splittedElements
          .filterIsInstance(PsiLanguageInjectionHost::class.java)
          .forEach { psiLanguageInjectionHost ->
            myInjectionsList.add(Trinity(psiLanguageInjectionHost,
                                         InjectedLanguage.create(injection.injectedLanguageId)!!,
                                         TextRange(0, psiLanguageInjectionHost.text.length)))
          }
      }
    }
    return myInjectionsList
  }

  private fun processInjection(lang: Language,
                               injectionList: List<Trinity<PsiLanguageInjectionHost, InjectedLanguage, TextRange>>,
                               finalContainingFile: PsiFile,
                               registrar: MultiHostRegistrar): Boolean {
    val injected = registerInjection(lang, injectionList, finalContainingFile, registrar)
    InjectorUtils.registerSupport(mySupport, false, injectionList[0].first, lang)
    val host = injectionList[0].getFirst()
    InjectorUtils.putInjectedFileUserData(host, lang, InjectedLanguageManager.FRANKENSTEIN_INJECTION, null)
    return injected
  }

  //we are overriding this method from InjectorUtils.registerInjection(...) to replace suffixes with our replacement
  private fun registerInjection(lang: Language,
                        injectionList: List<Trinity<PsiLanguageInjectionHost, InjectedLanguage, TextRange>>,
                        finalContainingFile: PsiFile,
                        registrar: MultiHostRegistrar) : Boolean {
    var injectionStarted = false
    for (t in injectionList) {
      val host = t.first
      if (host.containingFile !== finalContainingFile) continue

      val injectedLanguage = t.second
      val textRange = t.third

      if (!injectionStarted) {
        // TextMate language requires file extension
        if (!StringUtil.equalsIgnoreCase(lang.id, injectedLanguage.id)) {
          registrar.startInjecting(lang, StringUtil.toLowerCase(injectedLanguage.id))
        }
        else {
          registrar.startInjecting(lang)
        }
        injectionStarted = true
      }
      registrar.addPlace(injectedLanguage.prefix, getSuffix(host) , host, textRange)
    }
    if (injectionStarted)
      registrar.doneInjecting()

    return injectionStarted
  }

  /**
   * When an SQL query is splitted into parts by some CFML tag or expression, we should convert this CFML divider into a valid SQL query element.
   *
   */
  private fun getSuffix(host: PsiLanguageInjectionHost): String? {
    if (host is CfmlLeafPsiElement && host.parent is CfmlTagImpl) {
      val sibling = host.nextSibling ?: return null
      return when (sibling) {
        //in case of transforming <cfqueryparam> and <cfif> into a valid SQL parameter
        is CfmlTagImpl -> suffixForCfmlTag(sibling)
        //in case of transforming CfmlReference into a valid SQL parameter
        is LeafPsiElement -> suffixForCfmlLeaf(sibling)
        else -> null
      }
    }
    return null
  }

  //If an SQL query is splitted by a CfmlExpression we trying to resolve it or substitute with a dummy text 'parameter from expression'
  private fun suffixForCfmlLeaf(sibling: LeafPsiElement): String? {
    if (sibling.node.elementType == CfmlTokenTypes.START_EXPRESSION
        && sibling.nextSibling != null
        && sibling.nextSibling is CfmlReferenceExpression
        && sibling.nextSibling.nextSibling != null
        && sibling.nextSibling.nextSibling.node.elementType == CfmlTokenTypes.END_EXPRESSION) {
      val cfmlReferenceExpression = sibling.nextSibling as CfmlReferenceExpression
      val psiElement = cfmlReferenceExpression.resolve()
      if (psiElement != null && psiElement is CfmlAssignmentExpression.AssignedVariable) {
        val rightHandExpr = psiElement.rightHandExpr
        //if resolved expression is integer literal
        if (rightHandExpr is CfmlLiteralExpressionType && rightHandExpr.node?.elementType == CfmlElementTypes.INTEGER_LITERAL) {
          return rightHandExpr.text
        }
        //if resolved expression is a string literal
        if (rightHandExpr?.node?.elementType is CfmlStringLiteralExpressionType) {
          return rightHandExpr.text
        }
      }
      return "'parameter from expression'"
    }
    return null
  }

  //If an SQL query is splitted by <cfqueryparam> or <cfif> CFML tags, we substitute them with a dummy text
  private fun suffixForCfmlTag(cfmlTag: CfmlTagImpl): String? {
    return when (cfmlTag.name?.toLowerCase()) {
      "cfqueryparam" -> "'parameter from <cfqueryparam>'"
      "cfif" -> null
      else -> null
    }
  }

  private fun getHead(splittedElements: List<PsiElement>): PsiElement? {
    return if (splittedElements.size == 1)
      splittedElements[0].parent
    else
      PsiTreeUtil.findCommonParent(splittedElements)
  }

}
