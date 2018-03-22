// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.index

import com.intellij.javascript.nodejs.packageJson.PackageJsonDependencies
import com.intellij.javascript.nodejs.packages.NodePackageUtil
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
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.Processor
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.index.VueIndexBase.Companion.createJSKey

const val VUE = "vue"
@Suppress("PropertyName")
const val GLOBAL = "global"
const val LOCAL = "local"
const val MIXINS = "mixins"
const val EXTENDS = "extends"
const val DIRECTIVES = "directives"
const val GLOBAL_BINDING_MARK = "*"
const val VUE_CLASS_COMPONENT = "vue-class-component"
private const val INDEXED_ACCESS_HINT = "[]"
private const val DELIMITER = "#"

fun getForAllKeys(scope:GlobalSearchScope, key:StubIndexKey<String, JSImplicitElementProvider>): Collection<JSImplicitElement> {
  val keys = StubIndex.getInstance().getAllKeys(key, scope.project!!)
  return keys.mapNotNull { resolve(it, scope, key) }.flatMap { it.toList() }
}

fun resolve(name:String, scope:GlobalSearchScope, key:StubIndexKey<String, JSImplicitElementProvider>): Collection<JSImplicitElement>? {
  if (DumbService.isDumb(scope.project!!)) return null
  val normalized = normalizeNameForIndex(name)
  val indexKey = createJSKey(key)

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

fun hasVueClassComponentLibrary(project: Project): Boolean {
  if (DumbService.isDumb(project)) return false
  return CachedValuesManager.getManager(project).getCachedValue(project, {
    val packageJsonFiles = FilenameIndex.getVirtualFilesByName(project, PackageJsonUtil.FILE_NAME, GlobalSearchScope.projectScope(project))

    var recordedDependency = packageJsonFiles.any { PackageJsonUtil.getOrCreateData(it).isDependencyOfAnyType(VUE_CLASS_COMPONENT) }
    if (!recordedDependency) {
      val psiManager = PsiManager.getInstance(project)
      recordedDependency = packageJsonFiles.any {
        val psiFile = psiManager.findFile(it) ?: return@any false
        NodePackageUtil.hasAnyOfPluginsInstalled(psiFile, listOf(VUE_CLASS_COMPONENT))
      }
    }
    CachedValueProvider.Result(recordedDependency, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, ProjectRootModificationTracker.getInstance(project))
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