package com.intellij.openRewrite.micronaut

import com.intellij.buildsystem.model.unified.UnifiedCoordinates
import com.intellij.micronaut.getMicronautVersion
import com.intellij.micronaut.hasMicronautLibrary
import com.intellij.openRewrite.OpenRewriteRecipeLibraryContributor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project

private val RECIPE_REGEX: Regex = Regex("org\\.openrewrite\\.java\\.micronaut\\.Micronaut.*Migration")

internal class MnRecipeLibraryContributor : OpenRewriteRecipeLibraryContributor {
  override fun getRecipeLibraries(project: Project): List<UnifiedCoordinates> {
    if (hasMicronautLibrary(project)) {
      return listOf(UnifiedCoordinates("org.openrewrite.recipe", "rewrite-micronaut", "2.3.1"))
    }
    return emptyList()
  }

  override fun hasLibrary(module: Module): Boolean = hasMicronautLibrary(module)

  override fun isUpdateAvailable(module: Module): Boolean {
    val version = getMicronautVersion(module) ?: return false
    val major = version.substringBefore(".").toIntOrNull() ?: return false
    return major <= 3
  }

  override fun updateMatches(recipeName: String): Boolean = RECIPE_REGEX.matches(recipeName)
}
