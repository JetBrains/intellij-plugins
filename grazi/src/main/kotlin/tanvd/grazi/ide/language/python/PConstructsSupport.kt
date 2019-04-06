package tanvd.grazi.ide.language.python

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.jetbrains.python.psi.PyFile
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*

class PConstructsSupport : LanguageSupport() {
    override fun isSupported(file: PsiFile): Boolean {
        return file is PyFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (ident in file.filterFor<PsiNamedElement>()) {
            ident.name?.let {
                val indexOfName = ident.text.indexOf(it)

                if (indexOfName != -1) {
                    addAll(GraziSpellchecker.check(it).map { typo ->
                        typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(indexOfName), element = ident, shouldUseRename = true))
                    })
                }
            }
            ProgressManager.checkCanceled()
        }
    }
}
