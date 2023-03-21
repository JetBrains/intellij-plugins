// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.ide.fileTemplates.DefaultTemplatePropertiesProvider
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigServiceImpl
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexEx
import org.jetbrains.vuejs.context.getVueClassComponentDecoratorName
import org.jetbrains.vuejs.context.getVueClassComponentLibrary
import org.jetbrains.vuejs.context.hasVueFiles
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.VueComponentStylesIndex
import org.jetbrains.vuejs.libraries.VUE_CLASS_COMPONENT
import java.util.*

class VueDefaultTemplatePropertiesProvider : DefaultTemplatePropertiesProvider {
  override fun fillProperties(directory: PsiDirectory, props: Properties) {
    if (!hasVueFiles(directory.project) && !isVueContext(directory))
      return
    if (TypeScriptConfigServiceImpl.getNearestParentTsConfigs(directory.virtualFile, false).isNotEmpty()) {
      props["SCRIPT_LANG_ATTR"] = " lang='ts'"
      props["USE_DEFINE_COMPONENT"] = true
    }
    getDefaultVueStyleLang(directory.project)?.let {
      props["STYLE_LANG_ATTR"] = " lang='$it'"
    }
    props["CLASS_COMPONENT_LIB"] = getVueClassComponentLibrary(directory) ?: VUE_CLASS_COMPONENT
    props["CLASS_COMPONENT_DECORATOR"] = getVueClassComponentDecoratorName(directory)
  }

  private fun getDefaultVueStyleLang(project: Project): String? =
    if (DumbService.isDumb(project))
      null
    else
      FileBasedIndexEx.disableUpToDateCheckIn<String?, Throwable> {
        getAllStyles(project)
          .asSequence()
          .map { Pair(it, countFiles(it, project)) }
          .filter { it.second > 0 }
          .maxByOrNull { it.second }
          ?.first
          ?.takeIf { it.isNotBlank() }
      }

  private fun getAllStyles(project: Project): Set<String> {
    val styles = mutableSetOf<String>()
    FileBasedIndex.getInstance().processAllKeys(
      VueComponentStylesIndex.KEY,
      { style ->
        styles.add(style)
        true
      },
      project)
    return styles
  }

  private fun countFiles(style: String, project: Project): Int =
    FileBasedIndex.getInstance()
      .getContainingFiles(VueComponentStylesIndex.KEY, style, GlobalSearchScope.projectScope(project))
      .size

}