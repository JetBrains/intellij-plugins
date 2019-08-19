package tanvd.grazi.remote

import com.intellij.openapi.project.Project
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.language.Lang
import tanvd.grazi.remote.downloader.LangDownloader

object GraziRemote {
    private fun isLibExists(lib: String) = GraziPlugin.installationFolder.resolve("lib/$lib").exists()

    // TODO probably better to check all dependencies, but it will take a long time for resolving
    fun isAvailableLocally(lang: Lang) = isLibExists("language-${lang.shortCode}-${msg("grazi.languagetool.version")}.jar")

    /** Downloads [lang] to local storage */
    fun download(lang: Lang, project: Project? = null): Boolean {
        if (isAvailableLocally(lang)) return true

        return LangDownloader.download(lang, project)
    }

    /** Downloads all missing languages to local storage*/
    fun downloadMissing(project: Project?) = GraziConfig.get().missedLanguages.forEach { LangDownloader.download(it, project) }
}
