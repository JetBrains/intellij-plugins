package com.intellij.deno

import com.intellij.deno.lang.isJsonMetadataHashName
import com.intellij.deno.roots.useWorkspaceModel
import com.intellij.deno.service.DenoTypings
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.library.JSSyntheticLibraryProvider
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.roots.SyntheticLibrary.ExcludeFileCondition
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import java.nio.file.Path

internal val excludeCondition = ExcludeFileCondition { isDir, filename, _, _, _ ->
  !isDir &&
  !TypeScriptUtil.isDefinitionFile(filename) &&
  FileUtilRt.getExtension(filename).isNotEmpty() &&
  !isJsonMetadataHashName(filename)
}

class DenoLibrary(private val libs: List<VirtualFile>) : SyntheticLibrary("DenoLib", excludeCondition), ItemPresentation {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as DenoLibrary

    return libs == other.libs
  }

  override fun getSourceRoots(): Collection<VirtualFile> = libs
  override fun getPresentableText() = DenoBundle.message("deno.library.name")
  override fun hashCode(): Int = libs.hashCode()
  override fun getIcon(unused: Boolean) = DenoUtil.getDefaultDenoIcon()
}

class DenoLibraryProvider : AdditionalLibraryRootsProvider(), JSSyntheticLibraryProvider {
  override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
    if (useWorkspaceModel()) return emptyList()
    if (!useDenoLibrary(project)) return emptyList()

    val libs = getLibs(project)
    if (libs.isEmpty()) return emptyList()

    return listOf(DenoLibrary(libs))
  }

  private fun getLibs(project: Project): List<VirtualFile> {
    val settings = DenoSettings.getService(project)
    val typings = DenoTypings.getInstance(project)
    val deps = VirtualFileManager.getInstance().findFileByNioPath(Path.of(settings.getDenoCacheDeps()))
    val npm = VirtualFileManager.getInstance().findFileByNioPath(Path.of(settings.getDenoNpm()))
    val denoTypingsVirtualFile = typings.getDenoTypingsVirtualFile()

    return listOfNotNull(deps, npm, denoTypingsVirtualFile)
  }

  override fun getRootsToWatch(project: Project): Collection<VirtualFile> {
    return if (!useWorkspaceModel() && useDenoLibrary(project)) getLibs(project) else emptyList()
  }
}