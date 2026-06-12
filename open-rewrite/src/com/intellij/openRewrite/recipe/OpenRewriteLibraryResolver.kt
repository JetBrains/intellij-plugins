package com.intellij.openRewrite.recipe

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface OpenRewriteLibraryResolver {
  companion object {
    private val EP_NAME: ExtensionPointName<OpenRewriteLibraryResolver> =
      ExtensionPointName.create("com.intellij.openRewrite.libraryResolver")

    fun resolveDependencies(virtualFile: VirtualFile?, version: String, project: Project): List<String> {
      return EP_NAME.computeSafeIfAny { resolver ->
        resolver.resolveDependencies(virtualFile, version, project).takeIf { it.isNotEmpty() }
      } ?: emptyList()
    }
  }

  fun resolveDependencies(virtualFile: VirtualFile?, version: String, project: Project): List<String>
}