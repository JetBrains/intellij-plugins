// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.loader

import org.intellij.terraform.config.model.Argument
import org.intellij.terraform.config.model.HclType
import org.intellij.terraform.config.model.TfFunction
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.VariadicArgument
import org.intellij.terraform.config.model.array
import org.intellij.terraform.config.model.obj
import org.intellij.terraform.config.model.string
import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ObjectNode

internal const val FUNCTION_SIGNATURES_TYPE: String = "terraform-function-signatures-json"

/**
 * Loads the builtin functions from the output of `terraform metadata functions -json`.
 *
 * The output also lists each function under the `core::` namespace. The loader skips these names.
 */
internal class TfFunctionSignaturesLoader : VersionedMetadataLoader {
  override fun isSupportedVersion(version: String): Boolean = version == "1.0"

  override fun isSupportedType(type: String): Boolean = type == FUNCTION_SIGNATURES_TYPE

  override fun load(context: LoadContext, json: ObjectNode, fileName: String) {
    val model = context.model
    val signatures = json.obj("function_signatures") ?: return
    if (model.loaded.containsKey("functions")) {
      TfMetadataLoader.LOG.warn("Functions definitions already loaded from '${model.loaded["functions"]}'")
      return
    }
    model.loaded["functions"] = fileName
    for ((name, signature) in signatures.properties()) {
      if (name.contains("::") || signature !is ObjectNode) continue
      model.functions.add(parseFunction(context, name, signature))
    }
  }

  private fun parseFunction(context: LoadContext, name: String, signature: ObjectNode): TfFunction {
    val arguments = signature.array("parameters")
      ?.filterIsInstance<ObjectNode>()
      ?.map { Argument(parseType(context, it.get("type")), it.string("name")) }
      .orEmpty()
    val variadic = signature.obj("variadic_parameter")?.let {
      VariadicArgument(parseType(context, it.get("type")), it.string("name"))
    }
    return TfFunction(
      name.pool(context),
      parseType(context, signature.get("return_type")),
      *arguments.toTypedArray(),
      description = signature.string("description")?.pool(context),
      variadic = variadic,
    )
  }

  private fun parseType(context: LoadContext, node: JsonNode?): HclType =
    node?.let { TfProvidersSchemaParser.parseType(context, it) } ?: Types.Invalid
}
