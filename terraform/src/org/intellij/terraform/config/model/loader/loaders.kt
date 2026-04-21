// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.loader

import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.openapi.application.ApplicationManager
import org.intellij.terraform.config.model.ActionType
import org.intellij.terraform.config.model.BackendType
import org.intellij.terraform.config.model.DataSourceType
import org.intellij.terraform.config.model.EphemeralType
import org.intellij.terraform.config.model.HclType
import org.intellij.terraform.config.model.Hint
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.config.model.ProviderType
import org.intellij.terraform.config.model.ProvisionerType
import org.intellij.terraform.config.model.ResourceType
import org.intellij.terraform.config.model.TfFunction

class LoadingModel {
  val resources: MutableList<ResourceType> = mutableListOf()
  val dataSources: MutableList<DataSourceType> = mutableListOf()
  val providers: MutableList<ProviderType> = mutableListOf()
  val provisioners: MutableList<ProvisionerType> = mutableListOf()
  val backends: MutableList<BackendType> = mutableListOf()
  val functions: MutableList<TfFunction> = mutableListOf()
  val providerDefinedFunctions: MutableList<TfFunction> = mutableListOf()
  val ephemeralResources: MutableList<EphemeralType> = mutableListOf()
  val actions: MutableList<ActionType> = mutableListOf()
  val external: MutableMap<String, Additional> = linkedMapOf()
  val loaded: MutableMap<String, String> = linkedMapOf()
  data class Additional(val name: String, val description: String? = null, val hint: Hint? = null, val optional: Boolean? = null, val required: Boolean? = null)
}

class LoadContext(val pool: TfReusePool, val model: LoadingModel)

interface VersionedMetadataLoader {
  fun isSupportedVersion(version: String): Boolean
  fun isSupportedType(type: String): Boolean

  fun load(context: LoadContext, json: ObjectNode, fileName: String)
}

interface BaseLoader {
  val version:String

  fun parseSchemaElement(context: LoadContext, entry: Map.Entry<String, Any?>, fqnPrefix: String): PropertyOrBlockType
  fun parseSchemaElement(context: LoadContext, name: String, value: Any?, fqnPrefix: String): PropertyOrBlockType
  fun parseType(context: LoadContext, string: String?): HclType
}

internal fun warnOrFailInInternalMode(message: String) {
  val application = ApplicationManager.getApplication()
  if (application.isUnitTestMode || application.isInternal) {
    TfMetadataLoader.LOG.error(message)
    assert(false) { message }
  }
  TfMetadataLoader.LOG.warn(message)
}
