// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vueLoader

import com.intellij.lang.javascript.psi.resolve.JSModuleReferenceContributor
import com.intellij.openapi.paths.PathReference
import com.intellij.openapi.paths.PathReferenceProviderBase
import com.intellij.openapi.paths.PsiDynaReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.util.SmartList
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueLoaderPathReferenceProvider : PathReferenceProviderBase() {
  override fun createReferences(psiElement: PsiElement,
                                offset: Int,
                                text: String,
                                references: MutableList<in PsiReference>,
                                soft: Boolean): Boolean {
    // Vue-loader strips `~` and treats the rest as module reference (require parameter)
    // and if first char is `@` the whole string is treated as module reference.
    // https://vue-loader.vuejs.org/guide/asset-url.html#transform-rules

    val leadingTilde = text.startsWith("~")
    if (!(leadingTilde || text.startsWith("@")) || !isApplicable(psiElement)) return false

    val initialOffset = if (leadingTilde) 1 else 0

    val moduleRefs = JSModuleReferenceContributor.getReferences(
      text.substring(initialOffset), psiElement, offset + initialOffset, null, true)

    val pathSegments = pathSegments(text.substring(initialOffset), offset + initialOffset)

    // All module references are soft, so we need to convert them to hard references
    // for HtmlUnknownTargetInspection to highlighting issues. However, if there are any
    // not-per-segment references resolved, then we can return original soft references
    if (anyNonSegmentRefResolved(moduleRefs, pathSegments)) {
      references.clear()
      ContainerUtil.addAll(references, *moduleRefs)
      return true
    }
    val segmentRefs = createPerSegmentHardRefs(psiElement, moduleRefs, pathSegments)
    if (segmentRefs.isNotEmpty()) {
      references.clear()
      references.addAll(segmentRefs)
      return true
    }
    return false
  }

  override fun getPathReference(path: String, element: PsiElement): PathReference? {
    val list = SmartList<PsiReference>()
    createReferences(element, list, true)
    if (list.isEmpty()) return null

    val target = list[list.size - 1].resolve() ?: return null

    return object : PathReference(path, ResolveFunction.NULL_RESOLVE_FUNCTION) {
      override fun resolve(): PsiElement? {
        return target
      }
    }
  }

  private fun createPerSegmentHardRefs(psiElement: PsiElement,
                                       moduleRefs: Array<PsiReference>,
                                       pathSegments: Set<TextRange>): Collection<PsiReference> =
    moduleRefs.asSequence()
      .filter { pathSegments.contains(it.rangeInElement) }
      .groupingBy { it.rangeInElement }
      .aggregate { key, accumulator: PsiDynaReference<PsiElement>?, element, _ ->
        (accumulator ?: object : PsiDynaReference<PsiElement>(psiElement) {
          override fun isSoft(): Boolean = false
          override fun getRangeInElement(): TextRange = key
        }).also { it.addReference(element) }
      }.values

  private fun anyNonSegmentRefResolved(moduleRefs: Array<PsiReference>, pathSegments: Set<TextRange>): Boolean =
    moduleRefs.asSequence()
      .filter { !pathSegments.contains(it.rangeInElement) }
      .any {
        if (it is PsiPolyVariantReference) it.multiResolve(false).isNotEmpty()
        else it.resolve() != null
      }

  private fun isApplicable(psiElement: PsiElement): Boolean {
    return psiElement.containingFile
             .let {
               it.language == VueLanguage.INSTANCE
               && it.originalFile.virtualFile.let { vf -> vf == null || vf.fileType == VueFileType.INSTANCE }
             }
           && isVueContext(psiElement)
  }

  private fun pathSegments(path: String, offset: Int): Set<TextRange> {
    var segmentStart = 0
    return path.asSequence().mapIndexedNotNull { index, c ->
      when {
        c == '/' -> TextRange(segmentStart + offset, index + offset).also { segmentStart = index + 1 }
        index == path.length - 1 -> TextRange(segmentStart + offset, index + offset + 1)
        else -> null
      }
    }.toSet()
  }
}
