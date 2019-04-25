package tanvd.grazi.ide.language.kotlin

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*

class KConstructsSupport : LanguageSupport() {
    override fun isSupported(file: PsiFile): Boolean {
        return file is KtFile && GraziConfig.state.enabledSpellcheck
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (param in file.filterFor<KtParameter>()) {
            val function = (param.parent as? KtParameterList)?.parent as? KtNamedFunction
            if (function?.hasModifier(KtTokens.OVERRIDE_KEYWORD) == true) continue
            val paramName = param.name ?: continue
            param.text.ifContains(paramName) { index ->
                addAll(GraziSpellchecker.check(paramName).map { typo ->
                    typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(index),
                            element = param, shouldUseRename = true))
                })
            }
            ProgressManager.checkCanceled()
        }

        for (ident in file.filterFor<PsiNameIdentifierOwner>()) {
            if (ident is KtScript || (ident is KtModifierListOwner && ident.hasModifier(KtTokens.OVERRIDE_KEYWORD))) continue

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
