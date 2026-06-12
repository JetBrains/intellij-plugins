package com.intellij.openRewrite.run

import com.intellij.execution.configurations.RunProfileState

interface OpenRewriteState : RunProfileState {
  fun setRecipeArtifacts(artifacts: Collection<String>)
}