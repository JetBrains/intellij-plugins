// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.loader

import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.openapi.application.ApplicationManager
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.model.Function
import kotlin.collections.HashMap
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.arrayListOf
import kotlin.collections.linkedMapOf
import kotlin.collections.set

class LoadingModel(val external: Map<String, TypeModelProvider.Additional> = linkedMapOf()) {
  val resources: MutableList<ResourceType> = arrayListOf()
  val dataSources: MutableList<DataSourceType> = arrayListOf()
  val providers: MutableList<ProviderType> = arrayListOf()
  val provisioners: MutableList<ProvisionerType> = arrayListOf()
  val backends: MutableList<BackendType> = arrayListOf()
  val functions: MutableList<Function> = arrayListOf()

  val loaded: MutableMap<String, String> = linkedMapOf()
}

class ReusePool {
  private val strings: MutableMap<String, String> = HashMap()
  private val properties: MutableMap<PropertyType, PropertyType> = HashMap()
  private val blocks: MutableMap<BlockType, BlockType> = HashMap()
  private val hints: MutableMap<Hint, Hint> = HashMap()
  private val types: MutableMap<Type, Type> = HashMap()

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

  fun pool(v: BlockType): BlockType {
    var ret = blocks[v]
    if (ret != null) return ret
    ret = v
    blocks[ret] = ret
    return ret
  }

  fun pool(v: Hint): Hint {
    var ret = hints[v]
    if (ret != null) return ret
    ret = v
    hints[ret] = ret
    return ret
  }

  fun pool(t: Type): Type {
    var ret = types[t]
    if (ret != null) return ret
    ret = t
    types[ret] = ret
    return ret
  }
}

class LoadContext(val pool: ReusePool, val model: LoadingModel)

interface VersionedMetadataLoader {
  fun isSupportedVersion(version: String): Boolean
  fun isSupportedType(type: String): Boolean

  fun load(context: LoadContext, json: ObjectNode, file: String)
}

abstract class VersionedMetadataLoaderImpl(val version: String) : VersionedMetadataLoader {
  override fun isSupportedVersion(version: String): Boolean {
    return version == this.version
  }
}

interface BaseLoader {
  val version:String

  fun parseSchemaElement(context: LoadContext, entry: Map.Entry<String, Any?>, fqnPrefix: String): PropertyOrBlockType
  fun parseSchemaElement(context: LoadContext, name: String, value: Any?, fqnPrefix: String): PropertyOrBlockType
  fun parseType(context: LoadContext, string: String?): Type
}

internal fun String.pool(context: LoadContext): String = context.pool.pool(this)
internal fun PropertyType.pool(context: LoadContext): PropertyType = context.pool.pool(this)
internal fun BlockType.pool(context: LoadContext): BlockType = context.pool.pool(this)
internal fun Hint.pool(context: LoadContext): Hint = context.pool.pool(this)
internal fun Type.pool(context: LoadContext): Type = context.pool.pool(this)


internal fun warnOrFailInInternalMode(message: String) {
  val application = ApplicationManager.getApplication()
  if (application.isUnitTestMode || application.isInternal) {
    TerraformMetadataLoader.LOG.error(message)
    assert(false) { message }
  }
  TerraformMetadataLoader.LOG.warn(message)
}

internal fun failInInternalMode(message: String) {
  val application = ApplicationManager.getApplication()
  if (application.isUnitTestMode || application.isInternal) {
    TerraformMetadataLoader.LOG.error(message)
    assert(false) { message }
  }
}
