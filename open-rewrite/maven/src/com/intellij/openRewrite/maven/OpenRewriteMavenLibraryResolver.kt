package com.intellij.openRewrite.maven

import com.intellij.openRewrite.recipe.OpenRewriteLibraryResolver
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.idea.maven.dom.MavenDomUtil

internal class OpenRewriteMavenLibraryResolver : OpenRewriteLibraryResolver {
  override fun resolveDependencies(virtualFile: VirtualFile?, version: String, project: Project): List<String> {
    if (virtualFile == null) return emptyList()
    val model = MavenDomUtil.getMavenDomProjectModel(project, virtualFile) ?: return emptyList()
    return model.dependencyManagement.dependencies.dependencies.map { "${it.groupId}:${it.artifactId}:${it.version}" }
  }
}