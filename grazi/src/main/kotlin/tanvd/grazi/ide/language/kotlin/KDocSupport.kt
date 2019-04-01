package tanvd.grazi.ide.language.kotlin

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.psi.KtFile
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor


class KDocSupport : LanguageSupport {
    override fun isSupported(file: PsiFile): Boolean {
        return file is KtFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (doc in file.filterFor<KDocSection>()) {
            addAll(SanitizingGrammarChecker.default.check(doc.filterFor<KDocSection>()))

            ProgressManager.checkCanceled()
        }
    }
}
