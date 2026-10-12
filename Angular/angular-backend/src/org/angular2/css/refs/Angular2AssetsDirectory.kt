// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.css.refs

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.impl.SyntheticFileSystemItem
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.search.PsiFileSystemItemProcessor
import org.angular2.cli.config.AngularAssetPattern

/**
 * A directory of the application build output, as the `assets` of `angular.json` define it.
 * The children are the asset files and the directories that the build copies to [path].
 *
 * The class does not implement `FileReferenceResolver`.
 * Thus, `FileReferenceSet` does not add the parent directory of the referencing file as a context.
 */
internal class Angular2AssetsDirectory private constructor(
  project: Project,
  private val assets: List<AngularAssetPattern>,
  private val path: List<String>,
  private val parentDirectory: Angular2AssetsDirectory?,
) : SyntheticFileSystemItem(project) {

  /**
   * Creates the output root directory.
   */
  constructor(project: Project, assets: List<AngularAssetPattern>) : this(project, assets, emptyList(), null)

  override fun getName(): String =
    path.lastOrNull() ?: "/"

  override fun getParent(): PsiFileSystemItem? =
    parentDirectory

  override fun getVirtualFile(): VirtualFile? =
    assets.firstNotNullOfOrNull { findInputDirectory(it) }

  override fun getNavigationElement(): PsiElement =
    virtualFile?.let { myManager.findDirectory(it) } ?: this

  override fun isValid(): Boolean =
    assets.all { it.input.isValid }

  override fun processChildren(processor: PsiElementProcessor<in PsiFileSystemItem>): Boolean {
    val processedNames = mutableSetOf<String>()
    for (asset in assets) {
      if (asset.output.size > path.size) {
        // The asset output is below this directory.
        if (asset.output.subList(0, path.size) == path) {
          val name = asset.output[path.size]
          if (processedNames.add(name) && !processDirectory(processor, name)) return false
        }
        continue
      }
      val inputDirectory = findInputDirectory(asset) ?: continue
      val relativePath = path.subList(asset.output.size, path.size)
      for (child in inputDirectory.children) {
        val name = child.name
        if (child.isDirectory) {
          if (asset.acceptsDirectory(relativePath + name) && processedNames.add(name) && !processDirectory(processor, name)) return false
        }
        else if (asset.accepts(relativePath + name) && processedNames.add(name)) {
          if (processor is PsiFileSystemItemProcessor && !processor.acceptItem(name, false)) continue
          val file = myManager.findFile(child) ?: continue
          if (!processor.execute(file)) return false
        }
      }
    }
    return true
  }

  private fun processDirectory(processor: PsiElementProcessor<in PsiFileSystemItem>, name: String): Boolean =
    processFileSystemItem(processor, Angular2AssetsDirectory(myProject, assets, path + name, this))

  /**
   * @return the directory under [AngularAssetPattern.input] that the build copies to [path]
   */
  private fun findInputDirectory(asset: AngularAssetPattern): VirtualFile? {
    if (asset.output.size > path.size || asset.output != path.subList(0, asset.output.size)) return null
    return asset.input.findFileByRelativePath(path.subList(asset.output.size, path.size).joinToString("/"))
      ?.takeIf { it.isDirectory }
  }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is Angular2AssetsDirectory
    && other.myProject == myProject
    && other.assets == assets
    && other.path == path

  override fun hashCode(): Int =
    31 * assets.hashCode() + path.hashCode()

  override fun toString(): String =
    "Angular2AssetsDirectory(/${path.joinToString("/")})"
}
