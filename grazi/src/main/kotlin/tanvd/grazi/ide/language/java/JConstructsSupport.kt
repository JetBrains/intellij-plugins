package tanvd.grazi.ide.language.java

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*

class JConstructsSupport : LanguageSupport() {
    override fun isSupported(file: PsiFile): Boolean {
        return file is PsiJavaFile && GraziConfig.state.enabledSpellcheck
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (method in file.filterFor<PsiMethod>()) {
            val methodName = method.name
            method.text.ifContains(methodName) { index ->
                addAll(GraziSpellchecker.check(methodName).map { typo ->
                    typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(index),
                            element = method, shouldUseRename = true))
                })
            }
            ProgressManager.checkCanceled()
        }

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
