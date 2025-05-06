// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.Stack
import org.angular2.Angular2DecoratorUtil.DECLARATIONS_PROP
import org.angular2.Angular2DecoratorUtil.EXPORTS_PROP
import org.angular2.Angular2DecoratorUtil.IMPORTS_PROP
import org.angular2.entities.Angular2EntityUtils.forEachEntity
import org.jetbrains.annotations.NonNls
import java.util.*

/**
 * Also used for standalone declarables
 *
 *
 * TODO rename
 */
class Angular2ModuleResolver<T : PsiElement>(private val mySourceSupplier: () -> T,
                                             private val mySymbolCollector: SymbolCollector<T>) {

  val declarations: Set<Angular2Declaration>
    get() = getResolvedModuleList(DECLARATIONS_KEY).symbols

  val imports: Set<Angular2Entity>
    get() = getResolvedModuleList(IMPORTS_KEY).symbols

  val forwardRefImports: Set<Angular2Entity>
    get() = getResolvedModuleList(IMPORTS_KEY).forwardRefSymbols

  val exports: Set<Angular2Entity>
    get() = getResolvedModuleList(EXPORTS_KEY).symbols

  val isScopeFullyResolved: Boolean
    get() {
      if (!getResolvedModuleList(DECLARATIONS_KEY).isFullyResolved) {
        return false
      }
      val imports = getResolvedModuleList(IMPORTS_KEY)
      return imports.isFullyResolved &&
             imports.symbols.all { e -> e !is Angular2Module || e.areExportsFullyResolved() } &&
             areExportsFullyResolved()
    }

  val allExportedDeclarations: Set<Angular2Declaration>
    get() {
      val source = mySourceSupplier()
      return CachedValuesManager.getCachedValue(source) {
        val result = HashSet<Angular2Declaration>()
        val module = if (source is Angular2Module)
          source
        else
          Angular2EntitiesProvider.getModule(source)
        if (module != null) {
          val processedModules = HashSet<Angular2Module>()
          val moduleQueue = Stack(module)
          while (!moduleQueue.empty()) {
            val current = moduleQueue.pop()
            if (processedModules.add(current)) {
              forEachEntity(current.exports, moduleQueue::push, result::add)
            }
          }
        }
        Result.create<Set<Angular2Declaration>>(result, PsiModificationTracker.MODIFICATION_COUNT)
      }
    }

  fun areExportsFullyResolved(): Boolean {
    val exports = getResolvedModuleList(EXPORTS_KEY)
    return exports.isFullyResolved && exports.symbols.all { e -> e !is Angular2Module || e.areExportsFullyResolved() }
  }

  fun areDeclarationsFullyResolved(): Boolean {
    return getResolvedModuleList(DECLARATIONS_KEY).isFullyResolved
  }

  private inline fun <reified U : Angular2Entity> getResolvedModuleList(key: Key<CachedValue<Angular2ResolvedSymbolsSet<U>>>): Angular2ResolvedSymbolsSet<U> =
    getResolvedModuleList(key, U::class.java)

  private fun <U : Angular2Entity> getResolvedModuleList(key: Key<CachedValue<Angular2ResolvedSymbolsSet<U>>>,
                                                         entityClass: Class<U>): Angular2ResolvedSymbolsSet<U> {
    val source = mySourceSupplier()
    val symbolCollector = mySymbolCollector
    return CachedValuesManager.getCachedValue(source, key) {
      symbolCollector.collect(
        source,
        key.toString().removePrefix(KEYS_PREFIX),
        entityClass)
    }
  }

  interface SymbolCollector<T> {
    fun <U : Angular2Entity> collect(source: T,
                                     propertyName: String,
                                     symbolClazz: Class<U>): Result<Angular2ResolvedSymbolsSet<U>>
  }

  companion object {

    @NonNls
    val NG_MODULE_PROP: String = "ngModule"

    /**
     * See [ModuleWithProviders](https://angular.io/api/core/ModuleWithProviders)
     */
    @NonNls
    val MODULE_WITH_PROVIDERS_CLASS: String = "ModuleWithProviders"

    @NonNls
    private val KEYS_PREFIX = "angular.moduleResolver."
    private val DECLARATIONS_KEY = Key<CachedValue<Angular2ResolvedSymbolsSet<Angular2Declaration>>>(KEYS_PREFIX + DECLARATIONS_PROP)
    private val IMPORTS_KEY = Key<CachedValue<Angular2ResolvedSymbolsSet<Angular2Entity>>>(KEYS_PREFIX + IMPORTS_PROP)
    private val EXPORTS_KEY = Key<CachedValue<Angular2ResolvedSymbolsSet<Angular2Entity>>>(KEYS_PREFIX + EXPORTS_PROP)
  }
}
