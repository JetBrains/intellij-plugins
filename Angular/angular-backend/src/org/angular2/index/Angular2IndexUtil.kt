// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.ParameterizedCachedValueProvider
import com.intellij.util.ConcurrencyUtil
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object Angular2IndexUtil {

  fun multiResolve(project: Project,
                   index: StubIndexKey<in String, JSImplicitElementProvider>,
                   lookupKey: String,
                   processor: Processor<in JSImplicitElement>) {
    multiResolve(project, GlobalSearchScope.allScope(project), index, lookupKey, processor)
  }

  fun multiResolve(project: Project,
                   scope: GlobalSearchScope,
                   index: StubIndexKey<in String, JSImplicitElementProvider>,
                   lookupKey: String,
                   processor: Processor<in JSImplicitElement>) {
    ProgressManager.checkCanceled()
    StubIndex.getInstance().processElements(index, lookupKey, project, scope, JSImplicitElementProvider::class.java) { provider ->
      val indexingData = provider.indexingData
      if (indexingData != null) {
        val elements = indexingData.implicitElements
        if (elements != null) {
          for (element in elements) {
            if (element.qualifiedName == lookupKey
                && (!element.type.isFunction && isAngularRestrictions(element.userStringData))) {
              if (!processor.process(element)) return@processElements false
            }
          }
        }
      }
      true
    }
  }

  fun getAllKeys(index: ID<String, *>, project: Project): Collection<String> {
    val indexId = index.name
    val key = ConcurrencyUtil.cacheOrGet(ourCacheKeys, indexId, Key.create("angular2.index.$indexId"))
    val pair = Pair.create(project, index)
    return CachedValuesManager.getManager(project).getParameterizedCachedValue(project, key, AngularKeysProvider, false, pair)
  }

  fun resolveLocally(ref: JSReferenceExpression): List<PsiElement> {
    if (ref.qualifier == null && ref.referenceName != null) {
      return JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(ref.referenceName!!, ref)
    }
    return emptyList()
  }

  private val ourCacheKeys: ConcurrentMap<String, Key<ParameterizedCachedValue<Collection<String>, Pair<Project, ID<String, *>>>>> = ConcurrentHashMap()

  private fun isAngularRestrictions(restrictions: String?): Boolean {
    return restrictions == null || StringUtil.countChars(restrictions, ';') >= 3
  }

  object AngularKeysProvider : ParameterizedCachedValueProvider<Collection<String>, Pair<Project, ID<String, *>>> {
    override fun compute(projectAndIndex: Pair<Project, ID<String, *>>): CachedValueProvider.Result<Collection<String>> {
      val project = projectAndIndex.first
      val id = projectAndIndex.second
      val scope = GlobalSearchScope.allScope(project)
      val fileIndex = FileBasedIndex.getInstance()
      val stubIndex = StubIndex.getInstance()

      val keys = if (id is StubIndexKey<*, *>)
        stubIndex
          .getAllKeys(id as StubIndexKey<String, *>, project)
          .filter { key ->
            @Suppress("UNCHECKED_CAST")
            !stubIndex.processElements(id as StubIndexKey<String, PsiElement>, key, project, scope, PsiElement::class.java) { false }
          }
      else
        fileIndex
          .getAllKeys(id, project)
          .filter { key -> !fileIndex.processValues(id, key, null, { _, _ -> false }, scope) }
      return CachedValueProvider.Result.create(keys.sorted(), PsiManager.getInstance(project).modificationTracker)
    }
  }

}