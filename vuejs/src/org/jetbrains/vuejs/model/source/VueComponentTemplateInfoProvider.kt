// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.codeInsight.getFirstInjectedFile
import org.jetbrains.vuejs.codeInsight.getHostFile
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.index.VueUrlIndex
import org.jetbrains.vuejs.index.findTopLevelVueTag
import org.jetbrains.vuejs.model.*

class VueComponentTemplateInfoProvider : VueContainerInfoProvider {

  override fun getInfo(descriptor: VueSourceEntityDescriptor): VueContainerInfoProvider.VueContainerInfo =
    VueComponentTemplateInfo(descriptor.initializer ?: descriptor.clazz ?: descriptor.source)

  private class VueComponentTemplateInfo(private val element: PsiElement) : VueContainerInfoProvider.VueContainerInfo {
    override val template: VueTemplate<*>?
      get() {
        val element = element
        return CachedValuesManager.getCachedValue(element) {
          locateTemplateInTheSameVueFile(element)
          ?: locateTemplateInTemplateProperty(element)
          ?: locateTemplateInReferencingVueFile(element)
          ?: CachedValueProvider.Result.create(null as VueTemplate<*>?, element)
        }
      }
  }

  companion object {

    private fun createInfo(template: PsiElement?): VueTemplate<*>? =
      when (template) {
        is XmlFile -> VueFileTemplate(template)
        is XmlTag -> VueTagTemplate(template)
        else -> null
      }

    private fun locateTemplateInTheSameVueFile(source: PsiElement): CachedValueProvider.Result<VueTemplate<*>?>? {
      val context = source as? PsiFile ?: source.context!!
      return context.containingFile
        ?.asSafely<XmlFile>()
        ?.let { findTopLevelVueTag(it, HtmlUtil.TEMPLATE_TAG_NAME) }
        ?.let {
          CachedValueProvider.Result.create(locateTemplateInTemplateTag(it), context, context.containingFile)
        }
    }

    private fun locateTemplateInTemplateProperty(source: PsiElement): CachedValueProvider.Result<VueTemplate<*>?>? =
      (source as? JSObjectLiteralExpression)
        ?.findProperty(TEMPLATE_PROP)
        ?.value
        ?.let { expression ->
          // Inline template
          getFirstInjectedFile(expression)
            ?.let { return CachedValueProvider.Result.create(createInfo(it), source, it) }

          // Referenced template
          getReferencedTemplate(expression)
        }

    private fun getReferencedTemplate(expression: JSExpression): CachedValueProvider.Result<VueTemplate<*>?> {
      var directRefs = getTextIfLiteral(expression)?.startsWith("#") == true
      var referenceExpr = expression

      if (expression is JSCallExpression) {
        val args = expression.arguments
        if (args.size == 1) {
          referenceExpr = args[0]
          directRefs = true
        }
        else {
          return CachedValueProvider.Result.create(null, expression)
        }
      }

      var result: PsiElement? = null
      refs@ for (ref in referenceExpr.references) {
        val el = ref.resolve()
        if (directRefs) {
          if (el is PsiFile || el is XmlTag) {
            result = el
            break@refs
          }
        }
        else if (el is ES6ImportedBinding) {
          for (importedElement in el.findReferencedElements()) {
            if (importedElement is PsiFile) {
              result = importedElement
              break@refs
            }
          }
        }
      }
      return CachedValueProvider.Result.create(createInfo(result), expression, referenceExpr,
                                               VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }

    private fun locateTemplateInReferencingVueFile(source: PsiElement): CachedValueProvider.Result<VueTemplate<*>?>? {
      val file = getHostFile(source) ?: return null
      val name = file.viewProvider.virtualFile.name
      var result: CachedValueProvider.Result<VueTemplate<*>?>? = null

      StubIndex.getInstance().processElements(VueUrlIndex.KEY, name, source.project,
                                              GlobalSearchScope.projectScope(source.project), PsiElement::class.java) { element ->
        if (element is XmlAttribute
            && element.context?.let { it is XmlTag && it.name == HtmlUtil.SCRIPT_TAG_NAME } == true
            && element.valueElement?.references
              ?.any { it.resolve()?.containingFile == source.containingFile } == true) {
          result = CachedValueProvider.Result.create(
            element.containingFile
              ?.asSafely<XmlFile>()
              ?.let { findTopLevelVueTag(it, HtmlUtil.TEMPLATE_TAG_NAME) }
              ?.let { locateTemplateInTemplateTag(it) },
            element, element.containingFile,
            source, source.containingFile,
            VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
          )
          return@processElements false
        }
        true
      }
      return result
    }

    private fun locateTemplateInTemplateTag(tag: XmlTag): VueTemplate<*>? {
      val element = if (tag.hasSrcReference()) tag.tryResolveSrcReference() else tag
      return createInfo(element)
    }

  }

}
