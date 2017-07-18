package org.jetbrains.vuejs.index

import com.intellij.javascript.nodejs.packageJson.PackageJsonDependencies
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.Processor
import org.jetbrains.vuejs.VueFileType

val VUE = "vue"
val INDICES:MutableMap<StubIndexKey<String, JSImplicitElementProvider>, String> = mutableMapOf()

fun getAllKeys(scope:GlobalSearchScope, key:StubIndexKey<String, JSImplicitElementProvider>): Collection<JSImplicitElement> {
  val keys = StubIndex.getInstance().getAllKeys(key, scope.project!!)
  return keys.mapNotNull {
    resolve(it, scope, key)
  }
}

fun resolve(name:String, scope:GlobalSearchScope, key:StubIndexKey<String, JSImplicitElementProvider>): JSImplicitElement? {
  if (DumbService.isDumb(scope.project!!)) return null
  var result:JSImplicitElement? = null
  StubIndex.getInstance().processElements(key, name, scope.project!!, scope, JSImplicitElementProvider::class.java, Processor {
    val element = (it.indexingData?.implicitElements ?: emptyList()).firstOrNull { it.userString == INDICES[key] }
    result = element
    return@Processor element != null
  })
  return result
}

fun hasVue(project: Project): Boolean {
  if (DumbService.isDumb(project)) return false

  if (project.baseDir != null) {
    val packageJson = project.baseDir.findChild(PackageJsonUtil.FILE_NAME)
    if (packageJson != null) {
      val dependencies = PackageJsonDependencies.getOrCreate(project, packageJson)
      if (dependencies != null &&
          (dependencies.dependencies.containsKey(VUE) || dependencies.devDependencies.containsKey(VUE))) {
        return true
      }
    }
  }

  return CachedValuesManager.getManager(project)
    .getCachedValue(project, {
      val result = FileTypeIndex.containsFileOfType(VueFileType.INSTANCE, GlobalSearchScope.projectScope(project))
      CachedValueProvider.Result.create(result, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, ProjectRootModificationTracker.getInstance(project))
    })
}