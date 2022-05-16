// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.model.impl

import com.intellij.javascript.JSFileReference
import com.intellij.javascript.JSFileReference.IMPLICIT_EXTENSIONS
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.codeInsight.collectPropertiesRecursively
import org.jetbrains.vuejs.codeInsight.objectLiteralFor
import org.jetbrains.vuejs.libraries.vuex.VuexUtils
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MODULES
import org.jetbrains.vuejs.libraries.vuex.model.store.*
import org.jetbrains.vuejs.model.VueImplicitElement
import java.util.concurrent.ConcurrentHashMap

abstract class NuxtVuexContainer(override val source: PsiDirectory) : VuexContainer {

  override val state: Map<String, VuexStateProperty>
    get() = get(VuexUtils.STATE, ::VuexStatePropertyImpl)

  override val actions: Map<String, VuexAction>
    get() = get(VuexUtils.ACTIONS, ::VuexActionImpl)

  override val mutations: Map<String, VuexMutation>
    get() = get(VuexUtils.MUTATIONS, ::VuexMutationImpl)

  override val getters: Map<String, VuexGetter>
    get() = get(VuexUtils.GETTERS, ::VuexGetterImpl)

  override val modules: Map<String, VuexModule>
    get() = getFromCache(MODULES) {

      val result = mutableMapOf<String, VuexModule>()
      getJSFiles(source).forEach { (name, file) ->
        if (name !in RESERVED_NAMES) {
          result[name] = VuexModuleImpl(name, file, true)
        }
      }
      source.processChildren {
        if (it is PsiDirectory) {
          result[it.name] = NuxtVuexModule(it.name, it)
        }
        true
      }
      result.toMap()
    }

  override val initializer: JSObjectLiteralExpression?
    get() = null

  @Suppress("UNCHECKED_CAST")
  private fun <T> getFromCache(key: String, provider: () -> Map<String, T>): Map<String, T> {
    val source = source
    return CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result.create(ConcurrentHashMap<String, Map<String, *>>(),
                                        arrayOf(PsiModificationTracker.MODIFICATION_COUNT, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS))
    }.getOrPut(key) { provider() } as Map<String, T>
  }

  private fun <T> get(symbolKind: String, constructor: (name: String, source: JSProperty) -> T): Map<String, T> {
    return getFromCache(symbolKind) {
      val files = getJSFiles(source)
      files[symbolKind]?.let {
        buildFromExportedMembers(it, null, constructor)
      } ?: files[INDEX_FILE_NAME]?.let {
        buildFromExportedMembers(it, symbolKind, constructor)
      } ?: emptyMap()
    }
  }

  companion object {

    private const val INDEX_FILE_NAME = "index"

    private val RESERVED_NAMES: Set<String> = setOf(VuexUtils.STATE, VuexUtils.GETTERS, VuexUtils.ACTIONS, VuexUtils.MUTATIONS,
                                                    INDEX_FILE_NAME)

    private fun <T> buildFromExportedMembers(file: JSFile,
                                             exportName: String?,
                                             constructor: (name: String, source: JSProperty) -> T): Map<String, T> =
      ES6PsiUtil.resolveSymbolInModule(exportName ?: ES6PsiUtil.DEFAULT_NAME, file, file)
        .asSequence()
        .filter { it.isValidResult }
        .mapNotNull { objectLiteralFor(it.element) }
        .firstOrNull()
        ?.let {
          collectPropertiesRecursively(it)
        }?.associateBy({ it.first }, { constructor(it.first, it.second) })
      ?: emptyMap()


    private fun getJSFiles(dir: PsiDirectory): Map<String, JSFile> {
      val result = mutableMapOf<String, JSFile>()
      dir.processChildren { file ->
        if (file is JSFile) {
          val name = JSFileReference.getFileNameWithoutExtension(file.name, IMPLICIT_EXTENSIONS)
          if (!result.containsKey(name) || DialectDetector.isTypeScript(file)) {
            result[name] = file
          }
        }
        true
      }
      return result.toMap()
    }
  }
}

class NuxtVuexStore(storeDir: PsiDirectory) : NuxtVuexContainer(storeDir), VuexStore {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    return other is NuxtVuexStore
           && source == other.source
  }

  override fun hashCode(): Int {
    return source.hashCode()
  }
}


class NuxtVuexModule(override val name: String, source: PsiDirectory) : NuxtVuexContainer(source), VuexModule {

  override val isNamespaced: Boolean
    get() = true

  override fun getResolveTarget(namespace: String, qualifiedName: String): JSLocalImplicitElementImpl =
    VueImplicitElement(qualifiedName.substring(namespace.length), null, source,
                       JSImplicitElement.Type.Variable, true)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    return other is NuxtVuexModule
           && source == other.source
           && name == other.name
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + source.hashCode()
    return result
  }

}