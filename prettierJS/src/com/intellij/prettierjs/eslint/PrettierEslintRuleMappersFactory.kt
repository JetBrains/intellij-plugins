// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.eslint

import com.intellij.json.psi.*
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.linter.eslint.importer.EslintRuleMapper
import com.intellij.lang.javascript.linter.eslint.importer.EslintRuleMappersFactory
import com.intellij.lang.javascript.linter.eslint.importer.EslintSettingsConverter
import com.intellij.lang.javascript.ui.NodeModuleNamesUtil
import com.intellij.prettierjs.PrettierConfig.DEFAULT
import com.intellij.prettierjs.PrettierUtil
import com.intellij.psi.codeStyle.CodeStyleSettings

class PrettierEslintRuleMappersFactory : EslintRuleMappersFactory {

  override fun createMappers(): List<EslintRuleMapper> = listOf(
    PrettierRuleMapper()
  )

  private class PrettierRuleMapper : EslintRuleMapper("prettier/prettier") {
    override fun create(values: List<JsonValue>?, eslintConfig: EslintConfig): EslintSettingsConverter {
      val project = eslintConfig.configRoot.project
      val options = values?.getOrNull(0)
                      ?.let { it as? JsonObject }
                      ?.propertyList
                      ?.associateBy({ it.name }, {
                        when (val literal = it.value?.let { it as? JsonLiteral }) {
                          is JsonStringLiteral -> literal.value
                          is JsonNumberLiteral -> literal.value
                          is JsonBooleanLiteral -> literal.value
                          else -> null
                        }
                      }) ?: emptyMap()

      val usePrettierRc = values?.getOrNull(1)
                            ?.let { it as? JsonObject }
                            ?.findProperty("usePrettierrc")
                            ?.value
                            ?.let { it as? JsonBooleanLiteral }
                            ?.value ?: true

      val config = (
        if (usePrettierRc)
          eslintConfig.configRoot.containingFile?.virtualFile?.parent
            ?.let { directory ->
              PrettierUtil.findSingleConfigInDirectory(directory)
              ?: directory.findChild(NodeModuleNamesUtil.PACKAGE_JSON)
                ?.takeIf { PackageJsonUtil.isPackageJsonFile(it) }
            }
            ?.let { PrettierUtil.parseConfig(project, it) } ?: DEFAULT
        else DEFAULT)
        .mergeWith(options)

      return object : EslintSettingsConverter {
        override fun inSync(settings: CodeStyleSettings): Boolean = config.isInstalled(project)

        override fun apply(settings: CodeStyleSettings) {
          config.install(project)
        }
      }
    }
  }
}