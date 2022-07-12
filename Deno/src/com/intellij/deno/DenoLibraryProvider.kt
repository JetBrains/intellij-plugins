package com.intellij.deno

import com.intellij.deno.service.DenoTypings
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.library.JSSyntheticLibraryProvider
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class DenoLibrary(private val libs: List<VirtualFile>) :
  SyntheticLibrary("DenoLib",
                   ExcludeFileCondition { isDir, filename, _, _, _ ->
                     !isDir && !TypeScriptUtil.isDefinitionFile(
                       filename) && FileUtilRt.getExtension(filename).isNotEmpty()
                   }), ItemPresentation {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as DenoLibrary

    if (libs != other.libs) return false

    return true
  }

  override fun getSourceRoots(): Collection<VirtualFile> = libs
  override fun getPresentableText() = DenoBundle.message("deno.library.name")
  override fun hashCode(): Int = libs.hashCode()

  override fun getIcon(unused: Boolean): Icon? = null
}

class DenoLibraryProvider : AdditionalLibraryRootsProvider(), JSSyntheticLibraryProvider {
  override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
    val service = DenoSettings.getService(project)
    if (!service.isUseDeno()) return emptyList()

    val libs = getLibs(project, service)
    if (libs.isEmpty()) return emptyList()

    return listOf(DenoLibrary(libs))
  }

  private fun getLibs(project: Project, settings: DenoSettings): List<VirtualFile> {
    val denoPackages = settings.getDenoCacheDeps()
    val typings = DenoTypings.getInstance(project)
    val depsVirtualFile = LocalFileSystem.getInstance().findFileByPath(denoPackages)
    val denoTypingsVirtualFile = typings.getDenoTypingsVirtualFile()
    return listOfNotNull(depsVirtualFile, denoTypingsVirtualFile)
  }

  override fun getRootsToWatch(project: Project): Collection<VirtualFile> {
    val service = DenoSettings.getService(project)
    return if (!service.isUseDeno()) emptyList() else getLibs(project, service)
  }
}