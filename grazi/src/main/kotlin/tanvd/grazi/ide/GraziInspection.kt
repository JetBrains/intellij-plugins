package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.ide.quickfix.*
import tanvd.grazi.spellcheck.IdeaSpellchecker
import tanvd.grazi.utils.isSpellingTypo
import tanvd.grazi.utils.toSelectionRange
import tanvd.kex.buildList

class GraziInspection : LocalInspectionTool() {
    companion object {
        private fun getProblemMessage(fix: Typo): String {
            //language=HTML
            return """
            <html>
                <body>
                <p>${fix.info.rule.description}</p>
                ${if (!fix.isSpellingTypo) """
                    <p></p>
                    ${if (fix.info.rule.incorrectExamples.isNotEmpty()) """
                        <p><strong>Incorrect:</strong>&nbsp;${fix.info.rule.incorrectExamples.first().example}</p>""" else ""}
                    ${if (fix.info.rule.correctExamples.isNotEmpty()) """
                        <p><strong>Correct:</strong>&nbsp;${fix.info.rule.correctExamples.first().example}</p>""" else ""}    
                """.trimIndent() else ""}
                </body>
            </html>
            """.trimIndent()
        }

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

                manager.createProblemDescriptor(element, fix.toSelectionRange(), getProblemMessage(fix),
                        fix.info.category.highlight, isOnTheFly, *fixes.toTypedArray())
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
