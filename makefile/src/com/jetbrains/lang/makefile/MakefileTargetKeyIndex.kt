package com.jetbrains.lang.makefile

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.*
import com.jetbrains.lang.makefile.psi.MakefileTarget


val TARGET_INDEX_KEY = StubIndexKey.createIndexKey<String, MakefileTarget>("makefile.target.index")

class MakefileTargetIndex : StringStubIndexExtension<MakefileTarget>() {
  fun allTargets(project: Project): List<MakefileTarget> {
    val allTargets = mutableSetOf<String>()
    processAllKeys(project, CommonProcessors.CollectProcessor(allTargets))
    return allTargets.flatMap {
      getTargets(it, project, GlobalSearchScope.projectScope(project))
    }
  }

  override fun getKey(): StubIndexKey<String, MakefileTarget> = TARGET_INDEX_KEY

  @Deprecated("Base method is deprecated", ReplaceWith("getTargets(key, project, scope)"))
  override fun get(key: String, project: Project, scope: GlobalSearchScope): Collection<MakefileTarget> =
    getTargets(key, project, scope)

  fun getTargets(key: String, project: Project, scope: GlobalSearchScope): Collection<MakefileTarget> =
    StubIndex.getElements(TARGET_INDEX_KEY, key, project, scope, MakefileTarget::class.java)

  companion object {
    fun getInstance(): MakefileTargetIndex {
      return EP_NAME.findExtensionOrFail(MakefileTargetIndex::class.java)
    }
  }
}
