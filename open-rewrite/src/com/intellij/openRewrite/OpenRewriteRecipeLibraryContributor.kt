package com.intellij.openRewrite

import com.intellij.buildsystem.model.unified.UnifiedCoordinates
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project

interface OpenRewriteRecipeLibraryContributor {
  companion object {
    internal val EP_NAME: ExtensionPointName<OpenRewriteRecipeLibraryContributor> =
      ExtensionPointName.create("com.intellij.openRewrite.recipeLibraryContributor")
  }

  /**
   * Retrieves the list of additional OpenRewrite recipe libraries for the given project.
   *
   * @param project the project for which to retrieve the recipe libraries.
   * @return the list of recipe libraries maven coordinates.
   */
  fun getRecipeLibraries(project: Project): List<UnifiedCoordinates>

  fun hasLibrary(module: Module): Boolean = false

  fun isUpdateAvailable(module: Module): Boolean = false

  fun updateMatches(recipeName: String): Boolean = false
}