package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.ide.quickfix.*
import tanvd.grazi.spellcheck.IdeaSpellchecker
import tanvd.grazi.utils.buildList

class GraziInspection : LocalInspectionTool() {
    companion object {
        val EP_NAME = ExtensionPointName.create<LanguageSupport>("tanvd.grazi.languageSupport")

        private fun createProblemDescriptor(fix: Typo, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor {
            val end = if (fix.location.element!!.textLength >= fix.location.range.endInclusive + 1)
                fix.location.range.endInclusive + 1
            else
                fix.location.range.endInclusive

            val fixes = buildList<LocalQuickFix> {
                if (fix.info.rule.isDictionaryBasedSpellingRule) {
                    add(GraziAddWord(fix))
                }

                if (fix.location.shouldUseRename) {
                    add(GraziRenameTypo(fix))
                } else {
                    add(GraziReplaceTypo(fix))
                }

                add(GraziDisableRule(fix))
            }

            return manager.createProblemDescriptor(fix.location.element, TextRange.create(fix.location.range.start, end),
                    fix.info.description, fix.info.category.highlight,
                    isOnTheFly, *fixes.toTypedArray())
        }
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        IdeaSpellchecker.init(file.project)

        val result = mutableListOf<ProblemDescriptor>()
        for (ext in LanguageSupport.all.filter { it.isSupported(file) }) {
            val typos = ext.check(file)
            result += typos.map { createProblemDescriptor(it, manager, isOnTheFly) }
        }
        return result.toTypedArray()
    }
}
