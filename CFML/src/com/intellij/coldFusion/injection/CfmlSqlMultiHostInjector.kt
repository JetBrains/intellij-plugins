// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.CfmlUtil
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes
import com.intellij.coldFusion.model.parsers.CfmlElementTypes
import com.intellij.coldFusion.model.psi.*
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafPsiElement


/**
 * Adds SQL support into CFML cfquery tags.
 *
 * It creates a different set of injections for the whole cfquery content and for each cfif option tag.
 *
 * For example let's see how the next will be injected by SQL:
 *
 * <code><pre>
 * <cfquery datasource="test">
 *     <cfinclude template="test.cfm" />
 *     SELECT *                    <!---Common injection--->
 *     FROM Users u                <!---Common injection--->
 *     WHERE u.UserID = <cfqueryparam cfsqltype="cf_sql_integer" value="1"/>  <!---Common injection--->
 *     AND 1=1                     <!---Common injection--->
 *     <cfif apples IS apples>
 *         AND 2=2                 <!---Common injection--->
 *     <cfelseif apples IS apples>
 *         AND 3=3                 <!---Injection (2) --->
 *     <cfelse>
 *         AND 4=4                 <!---Injection (3) --->
 *     </cfif>
 *     AND 5=5                     <!---Common injection--->
 * </cfquery>
 * </pre></code>
 *
 * This example contains one common injection splitted into 4 hosts and two additional injections for {@code <cfelseif>} and {@code <cfelse>}
 * tags respectively. The common injection ignores {@code <cfinclude>} tag, resolves or substitutes {@code <cfqueryparam>} tag and takes the
 * first option for each {@code <cfif>} tag. If some {@code <cfif>} tag contains options by tags {@code <cfelseif>} or {@code <cfelse>}, new
 * injections are created for each options with a prefix and suffix according to common injection strategy.
 *
 */
class CfmlSqlMultiHostInjector(project: Project) : MultiHostInjector {

  private val CFQUERY_PARAM_DUMMY = "'parameter from <cfqueryparam>'"

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    val sqlLanguage = CfmlUtil.getSqlLanguage() ?: return
    if (context.isCfIfTag() && context.isTagInsideCfQuery()) {
      val cfIfTagOptionHosts = getCfIfTagOptionHosts(context)
      for (host in cfIfTagOptionHosts) {
        registerInjection(sqlLanguage, mapSplittedHostsToTextRanges(context, listOf(host)), context.containingFile, registrar)
      }
    }
    if (context.isCfQueryTag()) {
      val splitHosts = getCfQueryHosts(context)
      if (splitHosts.isEmpty()) return
      registerInjection(sqlLanguage, mapSplittedHostsToTextRanges(context, splitHosts), context.containingFile, registrar)
    }

  }

  override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(CfmlTagImpl::class.java)


  private fun getCfQueryHosts(context: PsiElement): List<PsiElement> {
    val filteredChildren = mutableListOf<PsiElement>()
    context.firstChild.traverse().forEach {
      if (it is CfmlLeafPsiElement) filteredChildren.add(it)
      if (it.isCfIfTag()) filteredChildren.addNotNull(getFirstCfIfValue(it as CfmlTagImpl))
    }
    return filteredChildren
  }

  //get hosts for optional cfif tags: cfelse and cfelseif
  private fun getCfIfTagOptionHosts(context: PsiElement): List<PsiElement> {
    return context.firstChild
      .traverse()
      .filter { (it.isCfElseTag() || it.isCfElseIfTag()) && it.nextSibling is CfmlLeafPsiElement }
      .map {
        it.nextSibling
      }
  }

  private fun mapSplittedHostsToTextRanges(head: PsiElement,
                                           splittedElements: List<PsiElement>): List<Triple<PsiElement, PsiLanguageInjectionHost, TextRange>> {
    return splittedElements
      .filterIsInstance(PsiLanguageInjectionHost::class.java)
      .map { psiLanguageInjectionHost ->
        Triple(head,
               psiLanguageInjectionHost,
               TextRange(0, psiLanguageInjectionHost.text.length))
      }
  }

  private fun registerInjection(lang: Language,
                                injectionList: List<Triple<PsiElement, PsiLanguageInjectionHost, TextRange>>,
                                finalContainingFile: PsiFile,
                                registrar: MultiHostRegistrar): Boolean {
    var injectionStarted = false
    for ((head, host, textRange) in injectionList) {
      if (host.containingFile !== finalContainingFile || !host.isValidHost) continue

      if (!injectionStarted) {
        registrar.startInjecting(lang)
        injectionStarted = true
      }
      if (head.isCfIfTag()) {
        registrar.addPlace(getPrefixForCfIfTag(head), getSuffixForCfIfTag(head), host, textRange)
      }
      else {
        registrar.addPlace(null, getSuffixForSplitElement(host), host, textRange)
      }

    }
    if (injectionStarted)
      registrar.doneInjecting()

    return injectionStarted
  }

  /**
   * When an SQL query is split into parts by some CFML tag or expression, we should convert this CFML divider into a valid SQL query element.
   */
  private fun getSuffixForSplitElement(host: PsiLanguageInjectionHost): String? {
    if (host is CfmlLeafPsiElement && host.parent is CfmlTagImpl) {
      val sibling = host.nextSibling ?: return null
      return when (sibling) {
      //in case of transforming <cfqueryparam>
        is CfmlTagImpl -> getSuffixForCfmlTag(sibling)
      //in case of transforming CfmlReference into a valid SQL parameter
        is LeafPsiElement -> getSuffixForCfmlExpression(sibling)
        else -> null
      }
    }
    return null
  }

  private fun getSuffixForCfIfTag(cfIfTag: PsiElement): String? {
    return cfIfTag.nextSibling?.traverse()?.map {
      if (it.isCfIfTag()) return@map getFirstCfIfValue(it as CfmlTagImpl)
      if (it.isCfQueryParamTag()) return@map CFQUERY_PARAM_DUMMY
      return@map it.text
    }?.joinToString()
  }

  private fun getPrefixForCfIfTag(cfIfTag: PsiElement): String {
    return cfIfTag.parent.firstChild.traverse(untilPsiElement = cfIfTag).map {
      if (it.isCfIfTag()) return@map getFirstCfIfValue(it as CfmlTagImpl)
      if (it.isCfQueryParamTag()) return@map CFQUERY_PARAM_DUMMY
      if (it is CfmlLeafPsiElement) return@map it.text
      return@map ""
    }.joinToString("")
  }

  //If an SQL query is split by a CfmlExpression we trying to resolve it or substitute with a dummy text 'parameter from expression'
  private fun getSuffixForCfmlExpression(sibling: LeafPsiElement): String? {
    val nextSibling = sibling.nextSibling
    if (sibling.node.elementType == CfmlTokenTypes.START_EXPRESSION
        && nextSibling != null
        && (nextSibling is CfmlReferenceExpression || nextSibling is CfmlFunctionCallExpression)
        && nextSibling.nextSibling != null
        && nextSibling.nextSibling.node.elementType == CfmlTokenTypes.END_EXPRESSION) {
      if (nextSibling is CfmlReferenceExpression) {
        val psiElement = nextSibling.resolve()
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
      } else if (nextSibling is CfmlFunctionCallExpression) { //if next sibling is CfmlFunctionalExpression
        //todo: add resolution for a function call here IDEA-205188
        return "'parameter from Functional Expression'"
      }
    }
    return null
  }

  //If an SQL query is split by <cfqueryparam> or <cfif> CFML tags, we substitute them with a dummy text
  private fun getSuffixForCfmlTag(cfmlTag: CfmlTagImpl): String? {
    return if (cfmlTag.isCfQueryParamTag()) CFQUERY_PARAM_DUMMY else null
  }


}