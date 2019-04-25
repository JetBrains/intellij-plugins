package tanvd.grazi.ide.language.python

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.jetbrains.python.psi.PyFile
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*

class PConstructsSupport : LanguageSupport() {
    override fun isSupported(file: PsiFile): Boolean {
        return file is PyFile && GraziConfig.state.enabledSpellcheck
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (ident in file.filterFor<PsiNamedElement>()) {
            val identName = ident.name ?: continue
            ident.text.ifContains(identName) { index ->
                addAll(GraziSpellchecker.check(identName).map { typo ->
                    typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(index),
                            element = ident, shouldUseRename = true))
                })
            }
            ProgressManager.checkCanceled()
        }
    }
}

