// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.psi.util.CachedValueProvider

class Angular2ResolvedSymbolsSet<T> private constructor(symbols: Set<T>, internal val isFullyResolved: Boolean) {
  internal val symbols: Set<T> = symbols.toSet()

  companion object {

    fun <T> createResult(entities: Set<T>,
                         isFullyResolved: Boolean,
                         dependency: Any): CachedValueProvider.Result<Angular2ResolvedSymbolsSet<T>> {
      return CachedValueProvider.Result.createSingleDependency(Angular2ResolvedSymbolsSet(entities, isFullyResolved), dependency)
    }

    fun <T> createResult(entities: Set<T>,
                         isFullyResolved: Boolean,
                         dependencies: Collection<*>): CachedValueProvider.Result<Angular2ResolvedSymbolsSet<T>> {
      return CachedValueProvider.Result.create(Angular2ResolvedSymbolsSet(entities, isFullyResolved), dependencies)
    }
  }
}