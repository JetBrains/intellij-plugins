package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.ide.quickfix.*
import tanvd.grazi.spellcheck.IdeaSpellchecker
import tanvd.grazi.utils.toSelectionRange
import tanvd.kex.buildList

class GraziInspection : LocalInspectionTool() {
    companion object {

        private fun createProblemDescriptor(fix: Typo, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor? {
            return fix.location.element?.let { element ->
                val fixes = buildList<LocalQuickFix> {
                    if (fix.info.rule.isDictionaryBasedSpellingRule) {
                        add(GraziAddWord(fix))
                    }

                    if (fix.fixes.isNotEmpty() && isOnTheFly) {
                        if (fix.location.shouldUseRename) {
                            add(GraziRenameTypo(fix))
                        } else {
                            add(GraziReplaceTypo(fix))
                        }
                    }

                    add(GraziDisableRule(fix))
                }

                manager.createProblemDescriptor(element, fix.toSelectionRange(), fix.info.description, fix.info.category.highlight, isOnTheFly, *fixes.toTypedArray())
            }
        }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement?) {
                element ?: return
                IdeaSpellchecker.init(element.project)

                for (ext in LanguageSupport.all.filter { it.isSupported(element.language) && it.isRelevant(element) }) {
                    val typos = ext.getFixes(element)
                    val problems = typos.mapNotNull { createProblemDescriptor(it, holder.manager, isOnTheFly) }
                    problems.forEach {
                        holder.registerProblem(it)
                    }
                }

                super.visitElement(element)
            }
        }
    }
}
