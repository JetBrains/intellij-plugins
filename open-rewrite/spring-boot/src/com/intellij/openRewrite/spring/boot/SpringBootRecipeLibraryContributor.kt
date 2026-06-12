package com.intellij.openRewrite.spring.boot

import com.intellij.buildsystem.model.unified.UnifiedCoordinates
import com.intellij.openRewrite.OpenRewriteRecipeLibraryContributor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.spring.boot.library.SpringBootLibraryUtil

private val RECIPE_REGEX: Regex = Regex("org\\.openrewrite\\.java\\.spring\\.boot[0-9]+\\.UpgradeSpringBoot.*")

internal class SpringBootRecipeLibraryContributor : OpenRewriteRecipeLibraryContributor {
  override fun getRecipeLibraries(project: Project): List<UnifiedCoordinates> {
    if (SpringBootLibraryUtil.hasSpringBootLibrary(project)) {
      return listOf(UnifiedCoordinates("org.openrewrite.recipe", "rewrite-spring", "5.4.0"))
    }
    return emptyList()
  }

  override fun hasLibrary(module: Module): Boolean = SpringBootLibraryUtil.hasSpringBootLibrary(module)

  override fun isUpdateAvailable(module: Module): Boolean =
    SpringBootLibraryUtil.isBelowVersion(module, SpringBootLibraryUtil.SpringBootVersion.VERSION_3_0_0)

  override fun updateMatches(recipeName: String): Boolean = RECIPE_REGEX.matches(recipeName)
}
