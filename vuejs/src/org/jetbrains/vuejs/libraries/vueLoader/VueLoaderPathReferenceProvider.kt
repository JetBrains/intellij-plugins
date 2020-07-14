// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vueLoader

import com.intellij.lang.javascript.psi.resolve.JSModuleReferenceContributor
import com.intellij.openapi.paths.PathReference
import com.intellij.openapi.paths.PathReferenceProviderBase
import com.intellij.openapi.paths.PsiDynaReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.SmartList
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueLoaderPathReferenceProvider : PathReferenceProviderBase() {

  override fun createReferences(psiElement: PsiElement,
                                offset: Int,
                                text: String,
                                references: MutableList<PsiReference>,
                                soft: Boolean): Boolean {
    val leadingTilde = text.startsWith("~")
    if ((leadingTilde || text.startsWith("@"))
        && isApplicable(psiElement)) {
      val initialOffset = if (leadingTilde) 1 else 0
      val moduleRefs = JSModuleReferenceContributor.getReferences(
        text.substring(initialOffset), psiElement, offset + initialOffset, null, true)
        .groupByTo(mutableMapOf()) { it.rangeInElement }

      // For HTML inspection we need to convert soft references for each path segment into strong ones
      var otherRefResolved = false
      val segmentRefs = pathSegments(text.substring(initialOffset), offset + initialOffset)
        .map { Pair(it, moduleRefs.remove(it)) }
        .filter { !it.second.isNullOrEmpty() }
        .map { (range, refs) ->
          object : PsiDynaReference<PsiElement>(psiElement) {
            override fun isSoft(): Boolean = otherRefResolved
            override fun getRangeInElement(): TextRange = range
          }.also { it.addReferences(refs) }
        }
        .toList()

      // Unless we have a resolved reference, which spans multiple segments
      val otherRefs = moduleRefs.asSequence()
        .map { (range, refs) ->
          object : PsiDynaReference<PsiElement>(psiElement) {
            override fun getRangeInElement(): TextRange = range
          }.also { it.addReferences(refs) }
        }
        .toList()
      otherRefResolved = otherRefs.any { it.multiResolve(false).isNotEmpty() }

      if (segmentRefs.isNotEmpty() || otherRefs.isNotEmpty()) {
        references.clear()
        references.addAll(segmentRefs)
        // If any of the other references is resolved, include them
        if (otherRefResolved) {
          references.addAll(otherRefs)
        }
        return true
      }
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

  private fun isApplicable(psiElement: PsiElement): Boolean {
    return psiElement.containingFile
             .let {
               it.language == VueLanguage.INSTANCE
               && it.originalFile.virtualFile.let { vf -> vf == null || vf.fileType == VueFileType.INSTANCE }
             }
           && isVueContext(psiElement)
  }

  private fun pathSegments(path: String, offset: Int): Sequence<TextRange> {
    var segmentStart = 0
    return path.asSequence().mapIndexedNotNull { index, c ->
      when {
        c == '/' -> TextRange(segmentStart + offset, index + offset).also { segmentStart = index + 1 }
        index == path.length - 1 -> TextRange(segmentStart + offset, index + offset + 1)
        else -> null
      }
    }
  }
}
