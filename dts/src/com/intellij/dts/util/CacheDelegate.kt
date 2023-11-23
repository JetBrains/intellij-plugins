package com.intellij.dts.util

import kotlin.reflect.KProperty

class CacheDelegate<T>(private val modificationCount: () -> Long, private val compute: () -> T) {
  private var cache: T? = null
  private var count: Long? = null

  @Synchronized
  operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
    val currentCount = modificationCount()

    return cache?.takeIf { count == currentCount } ?: run {
      count = currentCount
      compute().apply { cache = this }
    }
  }
}

fun <T> cached(modificationCount: () -> Long, compute: () -> T) = CacheDelegate(modificationCount, compute)