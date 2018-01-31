package org.jetbrains.vuejs.index

import com.intellij.javascript.nodejs.packageJson.PackageJsonDependencies
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
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
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.codeInsight.fromAsset

const val VUE = "vue"
@Suppress("PropertyName")
val INDICES:MutableMap<StubIndexKey<String, JSImplicitElementProvider>, String> = mutableMapOf()
const val GLOBAL = "global"
const val LOCAL = "local"
const val MIXINS = "mixins"
const val DIRECTIVES = "directives"
const val GLOBAL_BINDING_MARK = "*"
private const val INDEXED_ACCESS_HINT = "[]"
private const val DELIMITER = "#"

fun getForAllKeys(scope:GlobalSearchScope, key:StubIndexKey<String, JSImplicitElementProvider>): Collection<JSImplicitElement> {
  val keys = StubIndex.getInstance().getAllKeys(key, scope.project!!)
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

fun isGlobal(element: JSImplicitElement) = getVueIndexData(element).isGlobal

fun isGlobalExact(element: JSImplicitElement) = getVueIndexData(element).isGlobalExact()

fun createImplicitElement(name: String, provider: PsiElement, indexKey: String,
                          nameType: String? = null,
                          descriptor: PsiElement? = null,
                          isGlobal: Boolean = false): JSImplicitElementImpl {
  val normalized = normalizeNameForIndex(name)
  val nameTypeRecord = nameType ?: ""
  val asIndexed = descriptor as? JSIndexedPropertyAccessExpression
  var descriptorRef = asIndexed?.qualifier?.text ?: (descriptor as? JSReferenceExpression)?.text ?: ""
  if (asIndexed != null) descriptorRef += INDEXED_ACCESS_HINT
  return JSImplicitElementImpl.Builder(normalized, provider)
    .setUserString(indexKey)
    .setTypeString("${if(isGlobal) 1 else 0}$DELIMITER$nameTypeRecord$DELIMITER$descriptorRef$DELIMITER$name")
    .toImplicitElement()
}

private fun normalizeNameForIndex(name: String) = fromAsset(name.substringBeforeLast(GLOBAL_BINDING_MARK))

fun getVueIndexData(element : JSImplicitElement): VueIndexData {
  val typeStr = element.typeString ?: return VueIndexData(element.name, null, null, false, false)
  val originalName = typeStr.substringAfterLast(DELIMITER)
  val s = typeStr.substringBeforeLast(DELIMITER)
  val parts = s.split(DELIMITER)
  assert (parts.size == 3)

  val isGlobal = "1" == parts[0]
  val nameRef = parts[1]
  val descriptor = parts[2].substringBefore(INDEXED_ACCESS_HINT)
  val isIndexed = parts[2].endsWith(INDEXED_ACCESS_HINT)
  return VueIndexData(originalName, nameRef, descriptor, isIndexed, isGlobal)
}

class VueIndexData(val originalName: String,
                   val nameRef: String?, val descriptorRef: String?, val groupRegistration: Boolean, val isGlobal: Boolean) {
  fun isGlobalExact() = isGlobal && !originalName.endsWith(GLOBAL_BINDING_MARK)
}