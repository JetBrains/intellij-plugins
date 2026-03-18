// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.loader

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.ContainerType
import org.intellij.terraform.config.model.HclType
import org.intellij.terraform.config.model.PrimitiveType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.TupleType

class TfReusePool {
  private val strings: MutableMap<String, String> = HashMap()
  private val properties: MutableMap<PropertyType, PropertyType> = HashMap()

  private val blocks = createCache<BlockType, BlockType>(maximumSize = 2048)
  private val types = createCache<HclType, HclType>(maximumSize = 20)

  fun pool(v: String): String {
    var ret = strings[v]
    if (ret != null) return ret
    ret = v
    strings[ret] = ret
    return ret
  }

  fun pool(v: PropertyType): PropertyType {
    var ret = properties[v]
    if (ret != null) return ret
    ret = v
    properties[ret] = ret
    return ret
  }

  fun pool(block: BlockType): BlockType = blocks.get(block) { block }

  fun pool(type: HclType): HclType = types.get(type) { type }
}

private fun <K : Any, V : Any> createCache(maximumSize: Long): Cache<K, V> {
  return Caffeine.newBuilder()
    .maximumSize(maximumSize)
    .build()
}

internal fun String.pool(context: LoadContext): String = context.pool.pool(this)

internal fun PropertyType.pool(context: LoadContext): PropertyType = context.pool.pool(this)

internal fun BlockType.pool(context: LoadContext): BlockType = context.pool.pool(this)

internal fun HclType.pool(context: LoadContext): HclType = if (shouldPoolTypes()) context.pool.pool(this) else this

private fun HclType.shouldPoolTypes(): Boolean = when (this) {
  is PrimitiveType -> true
  is TupleType -> elements.all { it is PrimitiveType }
  is ContainerType<*> -> elements is PrimitiveType
  else -> false
}