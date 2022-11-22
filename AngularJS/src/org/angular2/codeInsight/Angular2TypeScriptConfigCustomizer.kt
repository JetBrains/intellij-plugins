// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.json.psi.JsonObject
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigCustomizer
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigParsingUtil.getObjectOfProperty
import com.intellij.openapi.util.Key

class Angular2TypeScriptConfigCustomizer : TypeScriptConfigCustomizer {
  companion object {
    @JvmStatic
    fun isStrictTemplates(config: TypeScriptConfig?): Boolean {
      return config?.getCustomOption(STRICT_TEMPLATES) ?: false
    }

    private val STRICT_TEMPLATES = Key.create<Boolean>("angularCompilerOptions.strictTemplates")
    private val STRICT_NULL_INPUT_TYPES = Key.create<Boolean>("angularCompilerOptions.strictNullInputTypes")
  }

  override fun parseJsonObject(jsonObject: JsonObject): Map<Key<*>, Any>? {
    val compilerOptions = getObjectOfProperty(jsonObject, "angularCompilerOptions")

    compilerOptions ?: return null

    val result = mutableMapOf<Key<*>, Any>()
    getBooleanValue(compilerOptions, "strictTemplates")?.let { result.put(STRICT_TEMPLATES, it) }
    getBooleanValue(compilerOptions, "strictNullInputTypes")?.let { result.put(STRICT_NULL_INPUT_TYPES, it) }

    return result
  }

  private fun getBooleanValue(jsonObject: JsonObject, name: String): Boolean? {
    val jsonProperty = jsonObject.findProperty(name)
    val jsonValue = jsonProperty?.value
    return if (jsonValue == null) null else java.lang.Boolean.parseBoolean(jsonValue.text)
  }
}