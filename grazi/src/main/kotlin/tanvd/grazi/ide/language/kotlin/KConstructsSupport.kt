package tanvd.grazi.ide.language.kotlin

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.SpellChecker

class KConstructsSupport : LanguageSupport {
    override fun isSupport(file: PsiFile): Boolean {
        return file is KtFile
    }

    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        val parameters = PsiTreeUtil.collectElementsOfType(file, KtParameter::class.java)
        val identifiers = PsiTreeUtil.collectElementsOfType(file, PsiNameIdentifierOwner::class.java)

        val result = ArrayList<LanguageSupport.Result>()
        for (param in parameters) {
            val function = (param.parent as? KtParameterList)?.parent as? KtNamedFunction
            when {
                function?.hasModifier(KtTokens.OVERRIDE_KEYWORD) == true -> {
                }
                else -> {
                    param.name?.let {
                        val indexOfName = param.text.indexOf(it)
                        val typos = SpellChecker.check(it)
                        result += typos.map { LanguageSupport.Result(it.withOffset(indexOfName), param) }
                    }
                }
            }
            ProgressManager.checkCanceled()
        }

        for (ident in identifiers) {
            when {
                ident is KtModifierListOwner && ident.hasModifier(KtTokens.OVERRIDE_KEYWORD) -> {
                }
                else -> {
                    ident.name?.let {
                        val indexOfName = ident.text.indexOf(it)
                        val typos = SpellChecker.check(it)
                        result += typos.map { LanguageSupport.Result(it.withOffset(indexOfName), ident) }
                    }
                }
            }
            ProgressManager.checkCanceled()
        }

        return result
    }
}
