// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.config

import com.intellij.json.psi.JsonObject
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigCustomizer
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigParsingUtil.getObjectOfProperty
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import org.angular2.codeInsight.config.Angular2TypeCheckingConfig.ControlFlowPreventingContentProjectionKind

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
    getConfigForPsiElement(psi)
      ?.getCustomOption(STRICT_INJECTION_PARAMETERS) == true

  // https://angular.io/guide/angular-compiler-options#strictinjectionparameters
  fun isStrictTemplates(psi: PsiElement?): Boolean =
    getConfigForPsiElement(psi)
      ?.getCustomOption(STRICT_TEMPLATES) == true

  // Following flags: https://angular.io/guide/template-typecheck#troubleshooting-template-errors
  fun isStrictInputAccessModifiers(psi: PsiElement?): Boolean =
    getConfigForPsiElement(psi)
      ?.getCustomOption(STRICT_INPUT_ACCESS_MODIFIERS) == true

  fun isStrictNullInputTypes(psi: PsiElement?): Boolean =
    getConfigForPsiElement(psi)
      .isStrictTemplateOption(STRICT_NULL_INPUT_TYPES)

  fun getTypeCheckingConfig(psi: PsiElement?): Angular2TypeCheckingConfig =
    with(getConfigForPsiElement(psi)) {
      val isStrictTemplates = this?.getCustomOption(STRICT_TEMPLATES) == true
      if (this == null)
        Angular2TypeCheckingConfig(
          checkTypeOfInputBindings = false,
          honorAccessModifiersForInputBindings = false,
          strictNullInputBindings = false,
          checkTypeOfAttributes = false,
          checkTypeOfDomBindings = false,
          checkTypeOfOutputEvents = false,
          checkTypeOfAnimationEvents = false,
          checkTypeOfDomEvents = false,
          checkTypeOfDomReferences = false,
          checkTypeOfNonDomReferences = false,
          enableTemplateTypeChecker = false,
          checkTypeOfPipes = false,
          applyTemplateContextGuards = false,
          strictSafeNavigationTypes = false,
          checkTemplateBodies = false,
          alwaysCheckSchemaInTemplateBodies = false,
          controlFlowPreventingContentProjection = ControlFlowPreventingContentProjectionKind.Warning,
          useContextGenericType = false,
          strictLiteralTypes = false,
          useInlineTypeConstructors = false,
          suggestionsForSuboptimalTypeInference = true,
          allowSignalsInTwoWayBindings = true, /* Angular 17.2.0+ */
          checkControlFlowBodies = false,
        )
      else
        Angular2TypeCheckingConfig(
          allowSignalsInTwoWayBindings = true, /* Angular 17.2.0+ */
          alwaysCheckSchemaInTemplateBodies = isStrictTemplates,
          applyTemplateContextGuards = getCustomOption(STRICT_INPUT_TYPES) ?: isStrictTemplates,
          checkControlFlowBodies = isStrictTemplates,
          checkTemplateBodies = isStrictTemplates,
          checkTypeOfAnimationEvents = getCustomOption(STRICT_OUTPUT_EVENT_TYPES) ?: isStrictTemplates,
          checkTypeOfAttributes = getCustomOption(STRICT_ATTRIBUTE_TYPES) ?: isStrictTemplates,
          checkTypeOfDomBindings = false,
          checkTypeOfDomEvents = getCustomOption(STRICT_DOM_EVENT_TYPES) ?: isStrictTemplates,
          checkTypeOfDomReferences = getCustomOption(STRICT_DOM_LOCAL_REF_TYPES) ?: isStrictTemplates,
          checkTypeOfInputBindings = getCustomOption(STRICT_INPUT_TYPES) ?: isStrictTemplates,
          checkTypeOfNonDomReferences = isStrictTemplates,
          checkTypeOfOutputEvents = getCustomOption(STRICT_OUTPUT_EVENT_TYPES) ?: isStrictTemplates,
          checkTypeOfPipes = isStrictTemplates,
          controlFlowPreventingContentProjection = ControlFlowPreventingContentProjectionKind.Warning,
          enableTemplateTypeChecker = false,
          honorAccessModifiersForInputBindings = getCustomOption(STRICT_INPUT_ACCESS_MODIFIERS) ?: false,
          strictLiteralTypes = getCustomOption(STRICT_LITERAL_TYPES) ?: isStrictTemplates,
          strictNullInputBindings = getCustomOption(STRICT_NULL_INPUT_TYPES) ?: isStrictTemplates,
          strictSafeNavigationTypes = getCustomOption(STRICT_SAFE_NAVIGATION_TYPES) ?: isStrictTemplates,
          suggestionsForSuboptimalTypeInference = !isStrictTemplates,
          useContextGenericType = getCustomOption(STRICT_CONTEXT_GENERICS) ?: isStrictTemplates,
          useInlineTypeConstructors = false,
        )
    }

  private fun getConfigForPsiElement(psi: PsiElement?): TypeScriptConfig? =
    psi?.let {
      TypeScriptConfigUtil.getConfigForPsiFile(psi.containingFile)
      ?: TypeScriptConfigService.Provider.get(psi.project).getPreferableOrParentConfig(psi.containingFile.originalFile.virtualFile)
    }

  private fun TypeScriptConfig?.isStrictTemplateOption(key: Key<Boolean>) =
    this != null && (getCustomOption(key) ?: (getCustomOption(STRICT_TEMPLATES) == true))

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