package tanvd.grazi.ide.language.kotlin

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.SpellChecker
import tanvd.grazi.utils.*

class KConstructsSupport : LanguageSupport {
    override fun isSupported(file: PsiFile): Boolean {
        return file is KtFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (param in file.filterFor<KtParameter>()) {
            val function = (param.parent as? KtParameterList)?.parent as? KtNamedFunction
            when {
                function?.hasModifier(KtTokens.OVERRIDE_KEYWORD) == true -> {
                }
                else -> {
                    param.name?.let {
                        val indexOfName = param.text.indexOf(it)
                        addAll(SpellChecker.check(it, file.project).map { typo ->
                            typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(indexOfName),
                                    element = param, shouldUseRename = true))
                        })
                    }
                }
            }
            ProgressManager.checkCanceled()
        }

        for (ident in file.filterFor<PsiNameIdentifierOwner>()) {
            when {
                ident is KtModifierListOwner && ident.hasModifier(KtTokens.OVERRIDE_KEYWORD) -> {
                }
                ident is KtScript -> {
                }
                else -> {
                    ident.name?.let {
                        val indexOfName = ident.text.indexOf(it)
                        if (indexOfName != -1) {
                            addAll(SpellChecker.check(it, file.project).map { typo ->
                                typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(indexOfName),
                                        element = ident, shouldUseRename = true))
                            })
                        }
                    }
                }
            }
            ProgressManager.checkCanceled()
        }
    }
}
