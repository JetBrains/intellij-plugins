// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.javascript.psi.JSPsiElementBase
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
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeDescriptor
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.*
import org.jetbrains.vuejs.codeInsight.findExpressionInAttributeValue
import org.jetbrains.vuejs.lang.expr.psi.VueJSSlotPropsExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import java.util.*
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

  private class VueTemplateElementScope constructor(root: PsiElement,
                                                    parent: VueTemplateElementScope?) : VueTemplateScope(parent) {

    private val elements = ArrayList<JSPsiElementBase>()

    private val myRange: TextRange = root.textRange

    init {
      if (parent != null) {
        assert(parent.myRange.contains(myRange))
      }
    }

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      elements.forEach { el -> consumer.accept(JSResolveResult(el)) }
    }

    fun add(element: JSPsiElementBase) {
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

  private open class VueBaseScopeBuilder internal constructor(private val myTemplateFile: PsiFile) : XmlRecursiveElementVisitor() {
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

    internal fun currentScope(): VueTemplateElementScope {
      return scopes.peek()
    }

    internal fun popScope() {
      scopes.pop()
    }

    internal fun pushScope(tag: XmlTag) {
      scopes.push(VueTemplateElementScope(tag, currentScope()))
    }

    internal fun addElement(element: JSPsiElementBase) {
      currentScope().add(element)
    }
  }

  private class VueTemplateScopeBuilder internal constructor(templateFile: PsiFile) : VueBaseScopeBuilder(templateFile) {

    override fun visitXmlTag(tag: XmlTag) {
      val tagHasVariables = tag.attributes
        .any { attribute ->
          (attribute.descriptor as? VueAttributeDescriptor)
            ?.getInfo()
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

    override fun visitXmlAttribute(attribute: XmlAttribute?) {
      (attribute?.descriptor as? VueAttributeDescriptor)
        ?.getInfo()
        ?.let { info ->
          when (info.kind) {
            VueAttributeKind.SLOT_SCOPE -> addSlotProps(attribute)
            VueAttributeKind.SCOPE -> addSlotProps(attribute)
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

    private fun addSlotProps(attribute: XmlAttribute) {
      findExpressionInAttributeValue(attribute, VueJSSlotPropsExpression::class.java)
        ?.getParameterList()
        ?.parameterVariables
        ?.forEach { addElement(it) }
    }

    private fun addVForVariables(attribute: XmlAttribute) {
      findExpressionInAttributeValue(attribute, VueJSVForExpression::class.java)
        ?.getVarStatement()
        ?.variables
        ?.forEach { addElement(it) }
    }
  }
}
