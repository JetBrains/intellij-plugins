// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.remote

import com.intellij.openapi.project.Project
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.GraziePlugin
import com.intellij.grazie.language.Lang

object GrazieRemote {
  private fun isLibExists(lib: String) = GraziePlugin.installationFolder.resolve("lib/$lib").exists()

  fun isAvailableLocally(lang: Lang) = isLibExists(lang.remote.file)

  /** Downloads [lang] to local storage */
  fun download(lang: Lang, project: Project? = null): Boolean {
    if (isAvailableLocally(lang)) return true

    return LangDownloader.download(lang, project)
  }

  /** Downloads all missing languages to local storage*/
  fun downloadMissing(project: Project?) = GrazieConfig.get().missedLanguages.forEach { LangDownloader.download(it, project) }
}
