package tanvd.grazi.remote

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
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
    fun downloadMissing(project: Project?) {
        val state = GraziConfig.get()

        if (state.hasMissedLanguages()) {
            state.enabledLanguages.filter { it.jLanguage == null }.forEach {
                LangDownloader.download(it, project)
            }

            if (state.nativeLanguage.jLanguage == null) {
                LangDownloader.download(state.nativeLanguage, project)
            }

            ProjectManager.getInstance().openProjects.forEach {
                DaemonCodeAnalyzer.getInstance(it).restart()
            }
        }
    }
}
