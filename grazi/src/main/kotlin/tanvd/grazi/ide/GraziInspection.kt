package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class GraziInspection : LocalInspectionTool() {
    companion object {
        val EP_NAME = ExtensionPointName.create<LanguageSupport>("tanvd.grazi.languageSupport")

        fun typoToProblemDescriptors(fix: Typo, element: PsiElement, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor {
            val end = if (element.textLength > fix.range.endInclusive + 1) fix.range.endInclusive + 1 else fix.range.endInclusive
            val range = TextRange.create(fix.range.start, end)
            val fixes = ArrayList<LocalQuickFix>()

            if (fix.category == Typo.Category.TYPOS) {
                val word = element.text.subSequence(fix.range).toString()
                fixes += GraziAddWordQuickFix(word, fix.hash)
            }
            if (fix.fix != null && fix.fix.isNotEmpty()) {
                fixes += GraziFixTypoQuickFix(fix.category.description, fix.fix)
            }

            return manager.createProblemDescriptor(element, range, fix.fullDescription, fix.category.highlight, isOnTheFly, *fixes.toTypedArray())
        }
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val result = mutableListOf<ProblemDescriptor>()
        for (ext in LanguageSupport.all.filter { it.isSupport(file) }) {
            val typos = ext.extract(file)
            result += typos.map { typoToProblemDescriptors(it.typo, it.element, manager, isOnTheFly) }
        }
        return result.toTypedArray()
    }
}
