// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.castSafelyTo
import com.intellij.xml.util.HtmlUtil.SCRIPT_TAG_NAME
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import org.jetbrains.vuejs.codeInsight.SRC_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.getFirstInjectedFile
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.index.TEMPLATE_PROP
import org.jetbrains.vuejs.index.VueIndexData
import org.jetbrains.vuejs.index.VueUrlIndex
import org.jetbrains.vuejs.index.findTopLevelVueTag
import org.jetbrains.vuejs.model.VueRegularComponent

class VueSourceComponent(sourceElement: JSImplicitElement,
                         clazz: JSClass?,
                         declaration: JSObjectLiteralExpression?,
                         private val indexData: VueIndexData?)
  : VueSourceContainer(sourceElement, clazz, declaration), VueRegularComponent {

  override val defaultName: String?
    get() = indexData?.originalName
            ?: getTextIfLiteral(initializer?.findProperty("name")?.value)

  override val template: PsiElement?
    get() {
      val element = initializer ?: source
      return CachedValuesManager.getCachedValue(element) {
        locateTemplateInTheSameVueFile(element)
        ?: locateTemplateInTemplateProperty(element)
        ?: locateTemplateInReferencingVueFile(element)
        ?: Result.create(null as PsiElement?, element)
      }
    }

  companion object {

    private fun locateTemplateInTheSameVueFile(source: PsiElement): Result<PsiElement>? {
      source.context
        ?.containingFile
        ?.castSafelyTo<XmlFile>()
        ?.let { findTopLevelVueTag(it, TEMPLATE_TAG_NAME) }
        ?.let {
          return Result.create(locateTemplateInTemplateTag(it), source.context, source.context!!.containingFile)
        }
      return null
    }

    private fun locateTemplateInTemplateProperty(source: PsiElement): Result<PsiElement>? {
      (source as? JSObjectLiteralExpression)
        ?.findProperty(TEMPLATE_PROP)
        ?.value
        ?.let { expression ->
          // Inline template
          getFirstInjectedFile(expression)
            ?.let { return Result.create(it, source, it) }

          // Referenced template
          return getReferencedTemplate(expression)
        }
      return null
    }

    internal fun getReferencedTemplate(expression: JSExpression): Result<PsiElement> {
      var directRefs = false
      var referenceExpr = expression

      if (expression is JSCallExpression) {
        val args = expression.arguments
        if (args.size == 1) {
          referenceExpr = args[0]
          directRefs = true
        }
        else {
          return Result.create(null, expression)
        }
      }

      var result: PsiFile? = null
      refs@ for (ref in referenceExpr.references) {
        val el = ref.resolve()
        if (directRefs) {
          if (el is PsiFile) {
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
      return Result.create(result, expression, referenceExpr, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }

    private fun locateTemplateInReferencingVueFile(source: PsiElement): Result<PsiElement>? {
      val file = getHostFile(source) ?: return null
      val name = file.viewProvider.virtualFile.name
      var result: Result<PsiElement>? = null

      StubIndex.getInstance().processElements(VueUrlIndex.KEY, name, source.project,
                                              GlobalSearchScope.projectScope(source.project), PsiElement::class.java) { element ->
        if (element is XmlAttribute
            && element.parent?.name == SCRIPT_TAG_NAME
            && element.valueElement?.references
              ?.any { it.resolve()?.containingFile == source.containingFile } == true) {
          result = Result.create(
            element.containingFile
              ?.castSafelyTo<XmlFile>()
              ?.let { findTopLevelVueTag(it, TEMPLATE_TAG_NAME) }
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

    internal fun getHostFile(context: PsiElement): PsiFile? {
      val original = CompletionUtil.getOriginalOrSelf(context)
      val hostFile = FileContextUtil.getContextFile(if (original !== context) original else context.containingFile.originalFile)
      return hostFile?.originalFile
    }

    private fun locateTemplateInTemplateTag(tag: XmlTag): PsiElement? {
      tag.getAttribute(SRC_ATTRIBUTE_NAME)?.let {
        return it.valueElement?.reference?.resolve()?.castSafelyTo<XmlFile>()
      }
      val child = PsiTreeUtil.findChildOfAnyType(tag, ASTWrapperPsiElement::class.java, XmlTag::class.java)
      if (child is XmlTag)
        return child
      else if (child is ASTWrapperPsiElement)
        return PsiTreeUtil.findChildOfType(child.firstChild, XmlTag::class.java)
      return null
    }
  }
}
