// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveResult
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ObjectUtils.notNull
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.*
import org.jetbrains.vuejs.codeInsight.findJSExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSScriptSetupExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSSlotPropsExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import java.util.function.Consumer

class VueTemplateElementsScopeProvider : VueTemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<VueTemplateScope> {
    val hostFile = CompletionUtil.getOriginalOrSelf(hostElement ?: element).containingFile
    val templateRootScope = CachedValuesManager.getCachedValue(hostFile) {
      CachedValueProvider.Result.create(
        VueTemplateScopeBuilder(hostFile).topLevelScope,
        PsiModificationTracker.MODIFICATION_COUNT)
    }
    return listOf(templateRootScope.findBestMatchingTemplateScope(notNull(hostElement, element))!!)
  }

  private class VueTemplateElementScope(root: PsiElement,
                                        parent: VueTemplateElementScope?) : VueTemplateScope(parent) {

    private val elements = ArrayList<JSPsiNamedElementBase>()

    private val myRange: TextRange = root.textRange

    init {
      if (parent != null) {
        assert(parent.myRange.contains(myRange))
      }
    }

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      elements.forEach { el -> consumer.accept(JSResolveResult(el)) }
    }

    fun add(element: JSPsiNamedElementBase) {
      elements.add(element)
    }

    fun findBestMatchingTemplateScope(element: PsiElement): VueTemplateElementScope? {
      if (!myRange.contains(element.textOffset)) {
        return null
      }
      var curScope: VueTemplateElementScope? = null
      var innerScope: VueTemplateElementScope? = this
      while (innerScope != null) {
        curScope = innerScope
        innerScope = null
        for (child in curScope.children) {
          if (child is VueTemplateElementScope && child.myRange.contains(element.textOffset)) {
            innerScope = child
            break
          }
        }
      }
      return curScope
    }
  }

  private open class VueBaseScopeBuilder(private val myTemplateFile: PsiFile) : XmlRecursiveElementVisitor() {
    private val scopes = Stack<VueTemplateElementScope>()

    val topLevelScope: VueTemplateElementScope
      get() {
        myTemplateFile.accept(this)
        assert(scopes.size == 1)
        return scopes.peek()
      }

    init {
      scopes.add(VueTemplateElementScope(myTemplateFile, null))
    }

    fun currentScope(): VueTemplateElementScope {
      return scopes.peek()
    }

    fun popScope() {
      scopes.pop()
    }

    fun pushScope(tag: XmlTag) {
      scopes.push(VueTemplateElementScope(tag, currentScope()))
    }

    fun addElement(element: JSPsiNamedElementBase) {
      currentScope().add(element)
    }
  }

  private class VueTemplateScopeBuilder(templateFile: PsiFile) : VueBaseScopeBuilder(templateFile) {

    override fun visitXmlTag(tag: XmlTag) {
      val tagHasVariables = tag.attributes
        .any { attribute ->
          attribute
            ?.let { VueAttributeNameParser.parse(it.name, it.parent) }
            ?.let { info ->
              info.kind === VueAttributeKind.SLOT_SCOPE
              || info.kind === VueAttributeKind.SCOPE
              || (info as? VueDirectiveInfo)?.directiveKind?.let {
                it === VueDirectiveKind.FOR
                || (it === VueDirectiveKind.SLOT && attribute.value != null)
              } ?: false
            }
          ?: false
        }

      if (tagHasVariables) {
        pushScope(tag)
      }
      super.visitXmlTag(tag)
      if (tagHasVariables) {
        popScope()
      }
    }

    override fun visitXmlAttribute(attribute: XmlAttribute) {
      attribute
        .let { VueAttributeNameParser.parse(it.name, it.parent) }
        .let { info ->
          when (info.kind) {
            VueAttributeKind.SLOT_SCOPE -> addSlotProps(attribute)
            VueAttributeKind.SCOPE -> addSlotProps(attribute)
            VueAttributeKind.SCRIPT_SETUP -> addScriptSetupParams(attribute)
            VueAttributeKind.DIRECTIVE ->
              when ((info as VueDirectiveInfo).directiveKind) {
                VueDirectiveKind.FOR -> addVForVariables(attribute)
                VueDirectiveKind.SLOT -> addSlotProps(attribute)
                else -> {
                }
              }
            else -> {
            }
          }
        }
    }

    private fun addScriptSetupParams(attribute: XmlAttribute) {
      attribute.valueElement
        ?.findJSExpression<VueJSScriptSetupExpression>()
        ?.getParameterList()
        ?.parameterVariables
        ?.forEach { addElement(it) }
    }

    private fun addSlotProps(attribute: XmlAttribute) {
      attribute.valueElement
        ?.findJSExpression<VueJSSlotPropsExpression>()
        ?.getParameterList()
        ?.parameterVariables
        ?.forEach { addElement(it) }
    }

    private fun addVForVariables(attribute: XmlAttribute) {
      attribute.valueElement
        ?.findJSExpression<VueJSVForExpression>()
        ?.getVarStatement()
        ?.variables
        ?.forEach { addElement(it) }
    }
  }
}
