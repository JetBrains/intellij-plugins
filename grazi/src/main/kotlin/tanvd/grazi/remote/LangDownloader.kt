// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.remote

import com.intellij.openapi.project.Project
import com.intellij.util.download.DownloadableFileService
import com.intellij.util.lang.UrlClassLoader
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.LangToolInstrumentation
import tanvd.grazi.utils.addUrls
import java.nio.file.Paths

object LangDownloader {
    private val downloader by lazy { DownloadableFileService.getInstance() }

    fun download(lang: Lang, project: Project?): Boolean {
        // check if language lib already loaded
        if (GraziRemote.isAvailableLocally(lang)) return true

        val result = downloader.createDownloader(listOf(downloader.createFileDescription(lang.remote.url, lang.remote.file)), msg("grazi.ui.settings.language.download.name", lang.displayName))
            .downloadFilesWithProgress(GraziPlugin.installationFolder.absolutePath + "/lib", project, null)

        // null if canceled or failed, zero result if nothing found
        if (result != null && result.isNotEmpty()) {
            (GraziPlugin.classLoader as UrlClassLoader).addUrls(result.map { Paths.get(it.presentableUrl).toUri().toURL() }.toList())
            LangToolInstrumentation.registerLanguage(lang)
            GraziConfig.update { state -> state.update() }
            return true
        }

        return false
    }
}
