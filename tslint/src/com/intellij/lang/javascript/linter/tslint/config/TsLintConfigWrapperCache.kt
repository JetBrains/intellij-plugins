package com.intellij.lang.javascript.linter.tslint.config

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.lang.javascript.linter.tslint.config.style.rules.TsLintConfigWrapper
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import com.intellij.psi.util.*


val RULES_CACHE_KEY = Key.create<ParameterizedCachedValue<TsLintConfigWrapper, PsiFile>>("tslint.cache.key.config.json")

class TsLintConfigWrapperCache(private val project: Project) {
  private val RULES_TO_APPLY: ParameterizedCachedValueProvider<TsLintConfigWrapper, PsiFile> = ParameterizedCachedValueProvider {
    if (it == null || PsiTreeUtil.hasErrorElements(it)) {
      return@ParameterizedCachedValueProvider CachedValueProvider.Result.create(null, it)
    }

    var jsonElement: JsonElement? = null
    try {
      jsonElement = JsonParser().parse(it.text)
    }
    catch (e: Exception) {
      //do nothing
    }
    if (jsonElement == null) {
      return@ParameterizedCachedValueProvider CachedValueProvider.Result.create(null, it)
    }

    val result = (if (jsonElement.isJsonObject) TsLintConfigWrapper(jsonElement.asJsonObject) else null)

    return@ParameterizedCachedValueProvider CachedValueProvider.Result.create(result, it)
  }


  companion object {
    fun getService(project: Project): TsLintConfigWrapperCache {
      return ServiceManager.getService(project, TsLintConfigWrapperCache::class.java)
    }
  }

  fun getWrapper(psiFile: PsiFile?): TsLintConfigWrapper? {
    if (psiFile == null) return null
    return CachedValuesManager.getManager(project)
             .getParameterizedCachedValue(psiFile, RULES_CACHE_KEY, RULES_TO_APPLY, false, psiFile) ?: return null
  }
}