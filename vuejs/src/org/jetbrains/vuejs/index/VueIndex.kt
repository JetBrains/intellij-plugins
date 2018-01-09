package org.jetbrains.vuejs.index

import com.intellij.javascript.nodejs.packageJson.PackageJsonDependencies
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.Processor
import org.jetbrains.vuejs.GLOBAL_BINDING_MARK
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.codeInsight.fromAsset

const val VUE = "vue"
@Suppress("PropertyName")
val INDICES:MutableMap<StubIndexKey<String, JSImplicitElementProvider>, String> = mutableMapOf()
const val GLOBAL = "global"
const val LOCAL = "local"
const val TYPE_DELIMITER = "#"
const val TYPE_MARKER = "@"
const val GLOBAL_COMP_COLLECTION = "**"

fun getForAllKeys(scope:GlobalSearchScope, key:StubIndexKey<String, JSImplicitElementProvider>): Collection<JSImplicitElement> {
  var keys = StubIndex.getInstance().getAllKeys(key, scope.project!!)
  return keys.mapNotNull { resolve(it, scope, key) }.flatMap { it.toList() }
}

fun resolve(name:String, scope:GlobalSearchScope, key:StubIndexKey<String, JSImplicitElementProvider>): Collection<JSImplicitElement>? {
  if (DumbService.isDumb(scope.project!!)) return null
  val normalized = normalizeNameForIndex(name)
  val indexKey = INDICES[key] ?: return null

  val result = mutableListOf<JSImplicitElement>()
  StubIndex.getInstance().processElements(key, normalized, scope.project!!, scope, JSImplicitElementProvider::class.java, Processor {
    provider: JSImplicitElementProvider? ->
      provider?.indexingData?.implicitElements
        // the check for name is needed for groups of elements, for instance:
        // directives: {a:..., b:...} -> a and b are recorded in 'directives' data.
        // You can find it with 'a' or 'b' key, but you should filter the result
        ?.filter { it.userString == indexKey && normalized == it.name }
        ?.forEach { result.add(it) }
    return@Processor true
  })
  return if (result.isEmpty()) null else result
}

fun hasVue(project: Project): Boolean {
  if (DumbService.isDumb(project)) return false

  return CachedValuesManager.getManager(project).getCachedValue(project, {
    var hasVue = false
    var packageJson:VirtualFile? = null
    if (project.baseDir != null) {
      packageJson = project.baseDir.findChild(PackageJsonUtil.FILE_NAME)
      if (packageJson != null) {
        val dependencies = PackageJsonDependencies.getOrCreate(project, packageJson)
        if (dependencies != null &&
            (dependencies.dependencies.containsKey(VUE) || dependencies.devDependencies.containsKey(VUE))) {
          hasVue = true
        }
      }
    }

    if (hasVue) {
      CachedValueProvider.Result.create(true, packageJson)
    } else {
      val result = FileTypeIndex.containsFileOfType(VueFileType.INSTANCE, GlobalSearchScope.projectScope(project))
      CachedValueProvider.Result.create(result, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, ProjectRootModificationTracker.getInstance(project))
    }
  })
}

fun createImplicitElement(name: String, provider: PsiElement, indexKey: String, type: String? = null): JSImplicitElementImpl {
  val normalized = normalizeNameForIndex(name)
  val typeRecord = if (type == null) "" else if (type.isEmpty()) TYPE_MARKER else type
  return JSImplicitElementImpl.Builder(normalized, provider)
    .setUserString(indexKey)
    .setTypeString("$typeRecord$TYPE_DELIMITER$name")
    .toImplicitElement()
}

private fun normalizeNameForIndex(name: String) = if (GLOBAL_COMP_COLLECTION == name) name
  else fromAsset(name.substringBeforeLast(GLOBAL_BINDING_MARK))

// TYPE_MARKER serves to differentiation between no-type (null) and not-null-empty-type (indicates literal global component)
fun getTypeString(element : JSImplicitElement): String? {
  val s = element.typeString?.substringBefore(TYPE_DELIMITER) ?: return null
  return if (TYPE_MARKER == s) "" else if (s.isEmpty()) null else s
}
fun getOriginalName(element : JSImplicitElement): String = element.typeString?.substringAfter(TYPE_DELIMITER) ?: element.name
