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
    fun getStrictTemplates(fullConfig: TypeScriptConfig?): Boolean {
      return fullConfig?.getCustomOption(STRICT_TEMPLATES) ?: false
    }

    val STRICT_TEMPLATES = Key.create<Boolean>("angularCompilerOptions.strictTemplates")
    val STRICT_NULL_INPUT_TYPES = Key.create<Boolean>("angularCompilerOptions.strictNullInputTypes")
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
    val compileOnSave = jsonObject.findProperty(name)
    val compileOnSaveValue = compileOnSave?.value
    return if (compileOnSaveValue == null) null else java.lang.Boolean.parseBoolean(compileOnSaveValue.text)
  }
}