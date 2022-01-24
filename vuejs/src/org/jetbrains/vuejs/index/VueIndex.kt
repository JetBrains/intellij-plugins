// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.packages.NodePackageUtil
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.Processor
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.index.VueIndexBase.Companion.createJSKey

const val VUE_MODULE: String = "vue"
const val VUE_CLI_SERVICE_MODULE: String = "@vue/cli-service"
const val VUE_INSTANCE_MODULE: String = "vue/types/vue"
const val VUETIFY_MODULE: String = "vuetify"
const val BOOTSTRAP_VUE_MODULE: String = "bootstrap-vue"
const val SHARDS_VUE_MODULE: String = "shards-vue"
const val VUE_CLASS_COMPONENT_MODULE: String = "vue-class-component"
const val COMPOSITION_API_MODULE: String = "@vue/composition-api"

@Suppress("PropertyName")
const val GLOBAL: String = "global"
const val LOCAL: String = "local"
const val GLOBAL_BINDING_MARK: String = "*"
internal const val INDEXED_ACCESS_HINT = "[]"
const val DELIMITER = "#"

fun getForAllKeys(scope: GlobalSearchScope, key: StubIndexKey<String, JSImplicitElementProvider>): Sequence<JSImplicitElement> {
  val keys = StubIndex.getInstance().getAllKeys(key, scope.project!!)
  return keys.asSequence().flatMap { resolve(it, scope, key) }
}

fun resolve(name: String, scope: GlobalSearchScope, key: StubIndexKey<String, JSImplicitElementProvider>): Collection<JSImplicitElement> {
  if (DumbService.isDumb(scope.project!!)) return emptyList()
  val normalized = normalizeNameForIndex(name)
  val indexKey = createJSKey(key)

  val result = mutableListOf<JSImplicitElement>()
  StubIndex.getInstance().processElements(
    key, normalized, scope.project!!, scope, JSImplicitElementProvider::class.java,
    Processor { provider: JSImplicitElementProvider? ->
      provider?.indexingData?.implicitElements
        // the check for name is needed for groups of elements, for instance:
        // directives: {a:..., b:...} -> a and b are recorded in 'directives' data.
        // You can find it with 'a' or 'b' key, but you should filter the result
        ?.filter { it.userString == indexKey && normalized == it.name }
        ?.forEach { result.add(it) }
      return@Processor true
    })
  return result
}

fun hasVueClassComponentLibrary(project: Project): Boolean {
  if (DumbService.isDumb(project)) return false
  return CachedValuesManager.getManager(project).getCachedValue(project) {
    val packageJsonFiles = FilenameIndex.getVirtualFilesByName(PackageJsonUtil.FILE_NAME, GlobalSearchScope.projectScope(project))

    var recordedDependency = packageJsonFiles.any { PackageJsonData.getOrCreate(it).isDependencyOfAnyType(VUE_CLASS_COMPONENT_MODULE) }
    if (!recordedDependency) {
      val psiManager = PsiManager.getInstance(project)
      recordedDependency = packageJsonFiles.any {
        val psiFile = psiManager.findFile(it) ?: return@any false
        NodePackageUtil.hasAnyOfPluginsInstalled(psiFile, listOf(VUE_CLASS_COMPONENT_MODULE))
      }
    }
    CachedValueProvider.Result(recordedDependency, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                               ProjectRootModificationTracker.getInstance(project))
  }
}

internal fun normalizeNameForIndex(name: String) = fromAsset(name.substringBeforeLast(GLOBAL_BINDING_MARK))

fun getVueIndexData(element: JSImplicitElement): VueIndexData? {
  val typeStr = element.userStringData ?: return null
  val originalName = typeStr.substringAfterLast(DELIMITER)
  val s = typeStr.substringBeforeLast(DELIMITER)
  val parts = s.split(DELIMITER)
  assert(parts.size == 3) {
    "Error with $element [name = ${element.name}, userString = ${element.userString}, typeString = $typeStr]"
  }

  val isGlobal = "1" == parts[0]
  val nameRef = parts[1]
  val descriptor = parts[2].substringBefore(INDEXED_ACCESS_HINT)
  val isIndexed = parts[2].endsWith(INDEXED_ACCESS_HINT)
  return VueIndexData(originalName, nameRef, descriptor, isIndexed, isGlobal)
}

class VueIndexData(val originalName: String,
                   val nameRef: String?, val descriptorRef: String?, val groupRegistration: Boolean, val isGlobal: Boolean)
