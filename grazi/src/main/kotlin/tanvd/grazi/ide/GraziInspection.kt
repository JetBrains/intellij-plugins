package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.model.Typo

class GraziInspection : LocalInspectionTool() {
    companion object {
        val EP_NAME = ExtensionPointName.create<LanguageSupport>("tanvd.grazi.languageSupport")

        fun typoToProblemDescriptors(fix: Typo, element: PsiElement, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor {
            val range = TextRange.create(fix.range.start, fix.range.endInclusive + 1)
            val quickFix = GraziQuickFix(fix.category.description, fix.fix ?: emptyList())
            return manager.createProblemDescriptor(element, range, fix.fullDescription,
                    fix.category.highlight, isOnTheFly, quickFix)
        }
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val result = mutableListOf<ProblemDescriptor>()
        for (ext in LanguageSupport.all) {
            val typos = ext.extract(file) ?: emptyList()
            result += typos.map { typoToProblemDescriptors(it.typo, it.element, manager, isOnTheFly) }
        }
        return result.toTypedArray()
    }
}
