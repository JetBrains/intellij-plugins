package org.jetbrains.vuejs.cli

import com.intellij.execution.filters.Filter
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.impl.ContentEntryImpl
import com.intellij.openapi.vfs.VirtualFile
import icons.VuejsIcons
import java.io.File

class VueCliProjectGenerator : NpmPackageProjectGenerator() {
  override fun getName() = "Vue.js"
  override fun getDescription() = "Simple CLI for scaffolding Vue.js projects"
  override fun filters(project: Project, baseDir: VirtualFile): Array<out Filter> = Filter.EMPTY_ARRAY
  override fun executable(path: String?) = "$path${File.separator}bin${File.separator}vue-init"
  override fun generatorArgs(project: Project, baseDir: VirtualFile) = arrayOf("webpack-simple", ".")

  override fun customizeModule(baseDir: VirtualFile, entry: ContentEntry?) {
    if (entry != null) {
      val project = (entry as ContentEntryImpl).rootModel.project
      JSRootConfiguration.getInstance(project).storeLanguageLevelAndUpdateCaches(JSLanguageLevel.ES6)
    }
  }

  override fun packageName() = "vue-cli"
  override fun presentablePackageName() = "Vue &CLI:"
  override fun getIcon() = VuejsIcons.Vue
}