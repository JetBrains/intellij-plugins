package tanvd.grazi

import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.language.Lang

object GraziLibResolver {
    fun isLibExists(lib: String) = GraziPlugin.installationFolder.resolve("lib/$lib").exists()

    // TODO probably better to check all dependencies, but it will take a long time for resolving
    fun hasAllLibs(lang: Lang) = GraziLibResolver.isLibExists("language-${lang.shortCode}-${msg("grazi.languagetool.version")}.jar")
}
