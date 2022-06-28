// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.index

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.packages.NodePackageUtil
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.util.text.StringUtil
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
const val VUE_INSTANCE_MODULE: String = "vue/types/vue"
const val VUETIFY_MODULE: String = "vuetify"
const val BOOTSTRAP_VUE_MODULE: String = "bootstrap-vue"
const val SHARDS_VUE_MODULE: String = "shards-vue"
const val VUE_CLASS_COMPONENT_MODULE: String = "vue-class-component"
const val COMPOSITION_API_MODULE: String = "@vue/composition-api"

const val GLOBAL: String = "global"
const val LOCAL: String = "local"
const val GLOBAL_BINDING_MARK: String = "*"
private const val INDEXED_ACCESS_HINT = "[]"
private const val DELIMITER = ','
private val DELIMITER_SPLIT_PATTERN = Regex("(?<!\\\\)$DELIMITER") // ignore \-escaped delimiters

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

data class VueIndexData(val originalName: String,
                        val nameQualifiedReference: String,
                        val descriptorQualifiedReference: String,
                        val indexedAccessUsed: Boolean,
                        val isGlobal: Boolean)

fun getVueIndexData(element: JSImplicitElement): VueIndexData? {
  val userStringData = element.userStringData ?: return null
  val parts = userStringData.split(DELIMITER_SPLIT_PATTERN)

  assert(parts.size == 4) {
    "Error with $element [name = ${element.name}, userString = ${element.userString}, userStringData = $userStringData, parts=$parts]"
  }

  val originalName = unescapePart(parts[0])
  val nameQualifiedReference = unescapePart(parts[1])
  val descriptorQualifiedReference = unescapePart(parts[2].substringBefore(INDEXED_ACCESS_HINT))
  val indexedAccessUsed = parts[2].endsWith(INDEXED_ACCESS_HINT)
  val isGlobal = parts[3] == "1"

  return VueIndexData(originalName, nameQualifiedReference, descriptorQualifiedReference, indexedAccessUsed, isGlobal)
}

fun serializeUserStringData(originalName: String,
                            nameQualifiedReference: String,
                            descriptorQualifiedReference: String,
                            indexedAccessUsed: Boolean,
                            isGlobal: Boolean): String {
  return buildString {
    append(escapePart(originalName))
    append(DELIMITER)
    append(escapePart(nameQualifiedReference))
    append(DELIMITER)
    append(escapePart(descriptorQualifiedReference))
    if (indexedAccessUsed) append(INDEXED_ACCESS_HINT)
    append(DELIMITER)
    append(if (isGlobal) "1" else "0")
  }
}

private fun escapePart(part: String): String {
  return StringUtil.escapeChar(part, DELIMITER)
}

private fun unescapePart(part: String): String {
  return StringUtil.unescapeChar(part, DELIMITER)
}
