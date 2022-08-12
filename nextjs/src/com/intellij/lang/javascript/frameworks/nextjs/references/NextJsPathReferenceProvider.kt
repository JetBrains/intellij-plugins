package com.intellij.lang.javascript.frameworks.nextjs.references

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.frameworks.amd.JSModuleReference
import com.intellij.lang.javascript.frameworks.html.getFixedDirectories
import com.intellij.lang.javascript.frameworks.modules.JSModuleFileReferenceSet
import com.intellij.lang.javascript.frameworks.JSRouteUtil
import com.intellij.openapi.paths.PathReference
import com.intellij.openapi.paths.PathReferenceProviderBase
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag

class NextJsPathReferenceProvider : PathReferenceProviderBase() {
  override fun createReferences(psiElement: PsiElement,
                                offset: Int,
                                text: String?,
                                references: MutableList<in PsiReference>,
                                soft: Boolean): Boolean {
    if (psiElement !is XmlAttributeValue || text == null || soft) return true
    val parent = psiElement.parent?.parent ?: return true
    if (parent !is XmlTag || !DialectDetector.isJSX(parent)) return true
    references.addAll(0, jsReferences(text, psiElement, offset).toList())

    return true
  }

  private fun jsReferences(text: String,
                           psiElement: PsiElement,
                           offset: Int) = NextJsFileReferenceSet(text, psiElement, offset).allReferences

  override fun getPathReference(path: String, element: PsiElement): PathReference? {
    if (element !is XmlAttributeValue) return null
    val parent = element.parent?.parent ?: return null
    if (parent !is XmlTag || !DialectDetector.isJSX(parent)) return null

    val jsReferences = jsReferences(path, element, 0)
    if (jsReferences.isEmpty()) return null
    val resolve = jsReferences.last().resolve() ?: return null
    return object : PathReference(path, ResolveFunction(null)) {
      override fun resolve(): PsiElement = resolve
    }
  }

  class NextJsFileReferenceSet(text: String, psiElement: PsiElement, offset: Int) :
    JSModuleFileReferenceSet(text, psiElement, offset, null, null) {

    override fun computeDefaultContexts(): Collection<PsiFileSystemItem> {
      val defaultContexts: Collection<PsiFileSystemItem> = super.computeDefaultContexts()
      val file: PsiFileSystemItem = element.containingFile?.originalFile ?: return defaultContexts

      val items = getFixedDirectories(file, JSRouteUtil.ROUTES)
      if (items.isEmpty()) return defaultContexts

      return items.toSet() + defaultContexts.toSet()
    }

    override fun createFileReference(textRange: TextRange?, i: Int, text: String?): FileReference {
      return object : JSModuleReference(text, i, textRange!!, this, null, isSoft, isUrlEncoded) {
        override fun handleElementRename(newElementName: String): PsiElement? {
          var newName = newElementName
          if (isLast) {
            newName = fixLastNameForRename(newName)
          }
          return super.handleElementRename(fixRelativePath(newName))
        }
      }
    }
  }
}