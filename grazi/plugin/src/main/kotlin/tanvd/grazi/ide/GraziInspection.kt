package tanvd.grazi.ide

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import kotlinx.html.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.ide.quickfix.GraziAddWord
import tanvd.grazi.ide.quickfix.GraziDisableRule
import tanvd.grazi.ide.quickfix.GraziRenameTypo
import tanvd.grazi.ide.quickfix.GraziReplaceTypo
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*
import tanvd.kex.buildList

class GraziInspection : LocalInspectionTool() {
    companion object : GraziStateLifecycle {
        private fun getProblemMessage(fix: Typo, isOnTheFly: Boolean): String {
            if (ApplicationManager.getApplication().isUnitTestMode) return fix.info.rule.id
            return html {
                if (fix.isSpellingTypo) {
                    +fix.info.rule.toDescriptionSanitized()
                } else {
                    if (fix.fixes.isNotEmpty()) {
                        p {
                            style = "padding-bottom: 10px;"
                            +"${fix.word} &rarr; ${fix.fixes.take(3).joinToString(separator = "/")}"
                            if (!isOnTheFly) nbsp()
                        }
                    }

                    p {
                        fix.info.incorrectExample?.let {
                            style = "padding-bottom: 8px;"
                        }

                        +fix.info.rule.toDescriptionSanitized()
                        if (!isOnTheFly) nbsp()
                    }

                    table {
                        cellpading = "0"
                        cellspacing = "0"

                        fix.info.incorrectExample?.let {
                            tr {
                                td {
                                    valign = "top"
                                    style = "padding-right: 5px; color: gray; vertical-align: top;"
                                    +msg("grazi.ui.settings.rules.rule.incorrect")
                                    if (!isOnTheFly) nbsp()
                                }
                                td {
                                    style = "width: 100%;"
                                    toIncorrectHtml(it)
                                    if (!isOnTheFly) nbsp()
                                }
                            }

                            if (it.corrections.any { !it.isNullOrBlank() }) {
                                tr {
                                    td {
                                        valign = "top"
                                        style = "padding-bottom: 5px; padding-top: 5px; padding-right: 5px; color: gray; vertical-align: top;"
                                        +msg("grazi.ui.settings.rules.rule.correct")
                                        if (!isOnTheFly) nbsp()
                                    }
                                    td {
                                        style = "padding-bottom: 5px; padding-top: 5px; width: 100%;"
                                        toCorrectHtml(it)
                                        if (!isOnTheFly) nbsp()
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

                manager.createProblemDescriptor(element, fix.toSelectionRange(), getProblemMessage(fix, isOnTheFly),
                        fix.info.category.highlight, isOnTheFly, *fixes.toTypedArray())
            }
        }

        override fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {
            if (prevState == newState) return

            ProjectManager.getInstance().openProjects.forEach {
                DaemonCodeAnalyzer.getInstance(it).restart()
            }
        }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement?) {
                element ?: return

                val typos = HashSet<Typo>()

                for (ext in LanguageSupport.allForLanguageOrAny(element.language).filter { it.isRelevant(element) }) {
                    typos.addAll(ext.getTypos(element))
                }

                if (GraziConfig.get().enabledSpellcheck) {
                    typos.addAll(GraziSpellchecker.getTypos(element))
                }

                typos.mapNotNull { createProblemDescriptor(it, holder.manager, isOnTheFly) }.forEach {
                    holder.registerProblem(it)
                }

                super.visitElement(element)
            }
        }
    }

    override fun getDisplayName() = "Grazi proofreading inspection"
}
