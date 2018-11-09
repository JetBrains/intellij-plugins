package com.intellij.lang.javascript.linter.tslint.codestyle.rules

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CommonCodeStyleSettings


interface TsLintRule {
  fun isAvailable(project: Project,
                  languageSettings: CommonCodeStyleSettings,
                  codeStyleSettings: JSCodeStyleSettings,
                  config: TsLintConfigWrapper): Boolean

  fun apply(project: Project,
            languageSettings: CommonCodeStyleSettings,
            codeStyleSettings: JSCodeStyleSettings,
            config: TsLintConfigWrapper)
}