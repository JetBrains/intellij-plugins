package com.intellij.openRewrite.quarkus

import com.intellij.buildsystem.model.unified.UnifiedCoordinates
import com.intellij.openRewrite.OpenRewriteRecipeLibraryContributor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.quarkus.QuarkusUtils

private val RECIPE_REGEX: Regex = Regex("org\\.openrewrite\\.quarkus\\.quarkus[0-9]+\\..*Migration")

internal class QsRecipeLibraryContributor : OpenRewriteRecipeLibraryContributor {
  override fun getRecipeLibraries(project: Project): List<UnifiedCoordinates> {
    if (QuarkusUtils.hasQuarkusLibrary(project)) {
      return listOf(UnifiedCoordinates("org.openrewrite.recipe", "rewrite-quarkus", "2.20"))
    }
    return emptyList()
  }

  override fun hasLibrary(module: Module): Boolean = QuarkusUtils.hasQuarkusLibrary(module)

  override fun isUpdateAvailable(module: Module): Boolean {
    val version = QuarkusUtils.getQuarkusVersion(module) ?: return false
    return version.startsWith("1.")
  }

  override fun updateMatches(recipeName: String): Boolean = RECIPE_REGEX.matches(recipeName)
}
