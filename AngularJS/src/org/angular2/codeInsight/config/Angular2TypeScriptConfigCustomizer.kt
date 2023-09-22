// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.config

import com.intellij.json.psi.JsonObject
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigCustomizer
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigParsingUtil.getObjectOfProperty
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

private val STRICT_TEMPLATES = Key.create<Boolean>("angularCompilerOptions.strictTemplates")
private val STRICT_NULL_INPUT_TYPES = Key.create<Boolean>("angularCompilerOptions.strictNullInputTypes")

fun isStrictTemplates(psi: PsiElement?): Boolean =
  psi?.containingFile?.let { file ->
    CachedValuesManager.getCachedValue(file) {
      val config = TypeScriptConfigUtil.getConfigForPsiFile(file)
      if (config == null) {
        CachedValueProvider.Result.create(
          false,
          PsiModificationTracker.MODIFICATION_COUNT,
          VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
        )
      }
      else {
        CachedValueProvider.Result.create(
          config.getCustomOption(STRICT_TEMPLATES),
          config.configFile,
          PsiModificationTracker.MODIFICATION_COUNT,
          VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
        )
      }
    }
  } == true

class Angular2TypeScriptConfigCustomizer : TypeScriptConfigCustomizer {
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