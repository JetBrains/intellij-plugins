// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.config

import com.intellij.json.psi.JsonObject
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigCustomizer
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigParsingUtil.getObjectOfProperty
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement

private val STRICT_INJECTION_PARAMETERS = Key.create<Boolean>("angularCompilerOptions.strictInjectionParameters")
private val STRICT_TEMPLATES = Key.create<Boolean>("angularCompilerOptions.strictTemplates")
private val STRICT_INPUT_TYPES = Key.create<Boolean>("angularCompilerOptions.strictInputTypes")
private val STRICT_INPUT_ACCESS_MODIFIERS = Key.create<Boolean>("angularCompilerOptions.strictInputAccessModifiers")
private val STRICT_NULL_INPUT_TYPES = Key.create<Boolean>("angularCompilerOptions.strictNullInputTypes")
private val STRICT_ATTRIBUTE_TYPES = Key.create<Boolean>("angularCompilerOptions.strictAttributeTypes")
private val STRICT_SAFE_NAVIGATION_TYPES = Key.create<Boolean>("angularCompilerOptions.strictSafeNavigationTypes")
private val STRICT_DOM_LOCAL_REF_TYPES = Key.create<Boolean>("angularCompilerOptions.strictDomLocalRefTypes")
private val STRICT_OUTPUT_EVENT_TYPES = Key.create<Boolean>("angularCompilerOptions.strictOutputEventTypes")
private val STRICT_DOM_EVENT_TYPES = Key.create<Boolean>("angularCompilerOptions.strictDomEventTypes")
private val STRICT_CONTEXT_GENERICS = Key.create<Boolean>("angularCompilerOptions.strictContextGenerics")
private val STRICT_LITERAL_TYPES = Key.create<Boolean>("angularCompilerOptions.strictLiteralTypes")

object Angular2Compiler {

  // https://angular.io/guide/angular-compiler-options#strictinjectionparameters
  fun isStrictInjectionParameters(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      ?.getCustomOption(STRICT_INJECTION_PARAMETERS) == true

  // https://angular.io/guide/angular-compiler-options#strictinjectionparameters
  fun isStrictTemplates(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      ?.getCustomOption(STRICT_TEMPLATES) == true

  // Following flags: https://angular.io/guide/template-typecheck#troubleshooting-template-errors
  fun isStrictInputAccessModifiers(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      ?.getCustomOption(STRICT_INPUT_ACCESS_MODIFIERS) == true

  fun isStrictInputTypes(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      .isStrictTemplateOption(STRICT_INPUT_TYPES)

  fun isStrictNullInputTypes(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      .isStrictTemplateOption(STRICT_NULL_INPUT_TYPES)

  fun isStrictAttributeTypes(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      .isStrictTemplateOption(STRICT_ATTRIBUTE_TYPES)

  fun isStrictSafeNavigationTypes(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      .isStrictTemplateOption(STRICT_SAFE_NAVIGATION_TYPES)

  fun isStrictDomLocalRefTypes(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      .isStrictTemplateOption(STRICT_DOM_LOCAL_REF_TYPES)

  fun isStrictDomEventTypes(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      .isStrictTemplateOption(STRICT_DOM_EVENT_TYPES)

  fun isStrictContextGenerics(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      .isStrictTemplateOption(STRICT_CONTEXT_GENERICS)

  fun isStrictLiteralTypes(psi: PsiElement?): Boolean =
    TypeScriptConfigUtil.getConfigForPsiFile(psi?.containingFile)
      .isStrictTemplateOption(STRICT_LITERAL_TYPES)

  private fun TypeScriptConfig?.isStrictTemplateOption(key: Key<Boolean>) =
    this != null && getCustomOption(STRICT_TEMPLATES) == true && getCustomOption(key) != false

}

class Angular2TypeScriptConfigCustomizer : TypeScriptConfigCustomizer {
  override fun parseJsonObject(jsonObject: JsonObject): Map<Key<*>, Any>? {
    val compilerOptions = getObjectOfProperty(jsonObject, "angularCompilerOptions")

    compilerOptions ?: return null

    val result = mutableMapOf<Key<*>, Any>()

    for (key in sequenceOf(
      STRICT_INJECTION_PARAMETERS, STRICT_TEMPLATES, STRICT_INPUT_TYPES, STRICT_INPUT_ACCESS_MODIFIERS, STRICT_NULL_INPUT_TYPES,
      STRICT_ATTRIBUTE_TYPES, STRICT_SAFE_NAVIGATION_TYPES, STRICT_DOM_LOCAL_REF_TYPES, STRICT_OUTPUT_EVENT_TYPES, STRICT_DOM_EVENT_TYPES,
      STRICT_CONTEXT_GENERICS, STRICT_LITERAL_TYPES,
    )) {
      getBooleanValue(compilerOptions, key.toString().removePrefix("angularCompilerOptions."))?.let { result.put(key, it) }
    }
    return result
  }

  private fun getBooleanValue(jsonObject: JsonObject, name: String): Boolean? {
    val jsonProperty = jsonObject.findProperty(name)
    val jsonValue = jsonProperty?.value
    return if (jsonValue == null) null else java.lang.Boolean.parseBoolean(jsonValue.text)
  }
}