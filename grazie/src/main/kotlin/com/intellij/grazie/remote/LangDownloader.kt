// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.remote

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.GraziePlugin
import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.grazie.language.Lang
import com.intellij.openapi.project.Project
import com.intellij.util.download.DownloadableFileService
import com.intellij.util.lang.UrlClassLoader
import org.languagetool.language.Language
import org.languagetool.language.Languages
import java.nio.file.Paths

object LangDownloader {
  private val downloader by lazy { DownloadableFileService.getInstance() }

  fun download(lang: Lang, project: Project?): Boolean {
    // check if language lib already loaded
    if (GrazieRemote.isAvailableLocally(lang)) return true

    val result = downloader.createDownloader(listOf(downloader.createFileDescription(lang.remote.url, lang.remote.file)),
                                             msg("grazie.ui.settings.language.download.name", lang.displayName))
      .downloadFilesWithProgress(GraziePlugin.installationFolder.absolutePath + "/lib", project, null)

    // null if canceled or failed, zero result if nothing found
    if (result != null && result.isNotEmpty()) {
      result.map { Paths.get(it.presentableUrl).toUri().toURL() }.forEach { (GraziePlugin.classLoader as UrlClassLoader).addURL(it) }
      lang.remote.langsClasses.forEach { className ->
        val qualifiedName = "org.languagetool.language.$className"
        if (Languages.get().all { it::class.java.canonicalName != qualifiedName }) {
          Languages.add(GraziePlugin.loadClass(qualifiedName)!!.newInstance() as Language)
        }
      }

      GrazieConfig.update { state -> state.update() }
      return true
    }

    return false
  }
}
