// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.json.psi.JsonObject
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigCustomizer
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigParsingUtil.getBooleanValue
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigParsingUtil.getObjectOfProperty

data class AngularCompilerOptions(val strictTemplates: Boolean, val strictNullInputTypes: Boolean)

class Angular2TypeScriptConfigCustomizer : TypeScriptConfigCustomizer<AngularCompilerOptions?> {
  companion object {
    @JvmStatic
    fun getAngularCompilerOptions(fullConfig: TypeScriptConfig?): AngularCompilerOptions? {
      return fullConfig?.getCustomOption(CUSTOM_KEY) as AngularCompilerOptions?
    }

    private const val CUSTOM_KEY = "Angular2TypeScriptConfigCustomizer"
  }

  override val customConfigKey get() = CUSTOM_KEY

  override fun parseJsonObject(jsonObject: JsonObject): AngularCompilerOptions? {
    val compilerOptions = getObjectOfProperty(jsonObject, "angularCompilerOptions")

    compilerOptions ?: return null

    val strictTemplates = getBooleanValue(compilerOptions, "strictTemplates", false)
    val strictNullInputTypes = getBooleanValue(compilerOptions, "strictNullInputTypes", false)

    return AngularCompilerOptions(strictTemplates, strictNullInputTypes)
  }

}