package com.intellij.deno

import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.library.JSSyntheticLibraryProvider
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class DenoLibrary(private val libs: List<VirtualFile>) : SyntheticLibrary(), ItemPresentation {
  
  override fun getExcludeFileCondition(): Condition<VirtualFile> = Condition {
    !it.isDirectory
    && !TypeScriptUtil.isDefinitionFile(it)
    && FileUtil.getExtension(it.nameSequence).isNotEmpty()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as DenoLibrary

    if (libs != other.libs) return false

    return true
  }

  override fun getSourceRoots(): Collection<VirtualFile> = libs
  override fun getPresentableText() = "deno@libs"
  override fun hashCode(): Int = libs.hashCode()

  override fun getLocationString(): String? = null
  override fun getIcon(unused: Boolean): Icon? = null
}

class DenoLibraryProvider : AdditionalLibraryRootsProvider(), JSSyntheticLibraryProvider {
  override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
    if (!DenoSettings.getService(project).isUseDeno()) return emptyList()

    val libs = getLibs()
    if (libs.isEmpty()) return emptyList()

    return listOf(DenoLibrary(libs))
  }

  private fun getLibs(): List<VirtualFile> {
    val denoPackages = DenoUtil.getDenoPackagesPath()
    val denoTypings = DenoUtil.getDenoTypings()
    val depsVirtualFile = LocalFileSystem.getInstance().findFileByPath(denoPackages)
    val denoTypingsVirtualFile = LocalFileSystem.getInstance().findFileByPath(denoTypings)
    return listOfNotNull(depsVirtualFile, denoTypingsVirtualFile)
  }

  override fun getRootsToWatch(project: Project): Collection<VirtualFile> {
    if (!DenoSettings.getService(project).isUseDeno()) return emptyList()
    return getLibs()
  }
}