package com.intellij.openRewrite.run

import com.intellij.execution.configurations.LocatableRunConfigurationOptions

open class OpenRewriteRunConfigurationOptions : LocatableRunConfigurationOptions() {
  var workingDirectory: String? by string()
  var dryRun: Boolean by property(false)
  var activeRecipes: String? by string()
  var activeStyles: String? by string()
  var configLocation: String? by string()
  var exclusions: String? by string()
  var plainTextMasks: String? by string()
  var libraryVersion: String? by string()
  var vmOptions: String? by string()
  var envs: MutableMap<String, String> by linkedMap()
  var passParentEnv: Boolean by property(true)
}
