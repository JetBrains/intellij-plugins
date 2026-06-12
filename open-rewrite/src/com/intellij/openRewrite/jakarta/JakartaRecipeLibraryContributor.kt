package com.intellij.openRewrite.jakarta

import com.intellij.buildsystem.model.unified.UnifiedCoordinates
import com.intellij.java.library.JavaLibraryUtil
import com.intellij.openRewrite.OpenRewriteRecipeLibraryContributor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project

private const val JAVAX_INJECT_MAVEN_COORDS: String = "javax.inject:javax.inject"
private const val JAKARTA_INJECT_MAVEN_COORDS: String = "jakarta.inject:jakarta.inject-api"
private const val JAVAX_PERSISTENCE_MAVEN_COORDS: String = "javax.persistence:javax.persistence-api"
private const val JAKARTA_PERSISTENCE_MAVEN_COORDS: String = "jakarta.persistence:jakarta.persistence-api"
private val RECIPE_REGEX: Regex = Regex("org\\.openrewrite\\.java\\.migrate\\.jakarta.*(Migration|JakartaEE).*")

internal class JakartaRecipeLibraryContributor : OpenRewriteRecipeLibraryContributor {
  override fun getRecipeLibraries(project: Project): List<UnifiedCoordinates> {
    return listOf(UnifiedCoordinates("org.openrewrite.recipe", "rewrite-migrate-java", "2.8.0"),
                  UnifiedCoordinates("org.openrewrite.recipe", "rewrite-java-dependencies", "1.6.0"),
                  UnifiedCoordinates("org.openrewrite.recipe", "rewrite-static-analysis", "2.0.0"),
                  UnifiedCoordinates("org.openrewrite.recipe", "rewrite-testing-frameworks", "3.0.0"),
                  UnifiedCoordinates("org.openrewrite.recipe", "rewrite-logging-frameworks", "3.0.0"))
  }

  override fun hasLibrary(module: Module): Boolean {
    return JavaLibraryUtil.hasLibraryJar(module, JAVAX_INJECT_MAVEN_COORDS) ||
           JavaLibraryUtil.hasLibraryJar(module, JAVAX_PERSISTENCE_MAVEN_COORDS)
  }

  override fun isUpdateAvailable(module: Module): Boolean {
    return hasLibraryJarExclusively(module, JAVAX_INJECT_MAVEN_COORDS, JAKARTA_INJECT_MAVEN_COORDS) ||
           hasLibraryJarExclusively(module, JAVAX_PERSISTENCE_MAVEN_COORDS, JAKARTA_PERSISTENCE_MAVEN_COORDS)
  }

  override fun updateMatches(recipeName: String): Boolean = RECIPE_REGEX.matches(recipeName)

  private fun hasLibraryJarExclusively(module: Module, coords: String, excludeCoords: String): Boolean {
    return JavaLibraryUtil.hasLibraryJar(module, coords) && !JavaLibraryUtil.hasLibraryJar(module, excludeCoords)
  }
}