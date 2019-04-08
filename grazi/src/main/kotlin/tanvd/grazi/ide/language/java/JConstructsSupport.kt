package tanvd.grazi.ide.language.java

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*

class JConstructsSupport : LanguageSupport() {
    override fun isSupported(file: PsiFile): Boolean {
        return file is PsiJavaFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (method in file.filterFor<PsiMethod>()) {
            method.name.let {
                val indexOfName = method.text.indexOf(it)

                if (indexOfName != -1) {
                    addAll(GraziSpellchecker.check(it).map { typo ->
                        typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(indexOfName), element = method, shouldUseRename = true))
                    })
                }
            }
            ProgressManager.checkCanceled()
        }

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
