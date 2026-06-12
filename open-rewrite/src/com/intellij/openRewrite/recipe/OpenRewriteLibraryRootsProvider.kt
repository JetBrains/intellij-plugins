package com.intellij.openRewrite.recipe

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFile

internal class OpenRewriteLibraryRootsProvider : AdditionalLibraryRootsProvider() {
  override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> =
    OpenRewriteRecipeService.getInstance(project).getLibraries()

  override fun getRootsToWatch(project: Project): Collection<VirtualFile> =
    OpenRewriteRecipeService.getInstance(project).getRoots()
}