package name.kropp.intellij.makefile

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import name.kropp.intellij.makefile.psi.MakefileTarget


val TARGET_INDEX_KEY = StubIndexKey.createIndexKey<String, MakefileTarget>("makefile.target.index")

object MakefileTargetIndex : StringStubIndexExtension<MakefileTarget>() {
  override fun getKey(): StubIndexKey<String, MakefileTarget> = TARGET_INDEX_KEY

  override fun get(key: String, project: Project, scope: GlobalSearchScope): Collection<MakefileTarget> =
      StubIndex.getElements(TARGET_INDEX_KEY, key, project, scope, MakefileTarget::class.java)
}