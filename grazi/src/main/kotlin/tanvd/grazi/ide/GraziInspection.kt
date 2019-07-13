package tanvd.grazi.ide

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import kotlinx.html.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.ide.msg.GraziAppLifecycle
import tanvd.grazi.ide.quickfix.*
import tanvd.grazi.ide.ui.msg
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*
import tanvd.kex.buildList

class GraziInspection : LocalInspectionTool() {
    companion object : GraziAppLifecycle {
        private fun getProblemMessage(fix: Typo): String {
            if (ApplicationManager.getApplication().isUnitTestMode) return fix.info.rule.id
            return html {
                if (fix.isSpellingTypo) {
                    if (fix.info.rule.description.length > 50) {
                        style = "width: 300px;"
                    }

                    +fix.info.rule.toDescriptionSanitized()
                } else {
                    div {
                        style = "margin-bottom: 3px;"
                        if (fix.info.rule.description.length > 50 || fix.info.incorrectExample?.example?.length ?: 0 > 50) {
                            style += "width: 300px;"
                        }

                        table {
                            if (fix.fixes.isNotEmpty()) {
                                tr {
                                    td {
                                        colSpan = "2"
                                        style = "padding-bottom: 3px;"
                                        +"${fix.word} &rarr; ${fix.fixes.take(3).joinToString(separator = "/")}"
                                    }
                                }
                            }

                            tr {
                                td {
                                    colSpan = "2"
                                    +fix.info.rule.toDescriptionSanitized()
                                }
                            }
                        }

                        table {
                            fix.info.incorrectExample?.let {
                                tr {
                                    style = "padding-top: 5px;"
                                    td {
                                        style = "color: gray;"
                                        +msg("grazi.ui.settings.rules.rule.incorrect")
                                    }
                                    td {
                                        style = "text-align: left"
                                        toIncorrectHtml(it)
                                    }
                                }

                                if (it.corrections.any { !it.isNullOrBlank() }) {
                                    tr {
                                        style = "padding-top: 5px;"
                                        td {
                                            style = "color: gray;"
                                            +msg("grazi.ui.settings.rules.rule.correct")
                                        }
                                        td {
                                            style = "text-align: left"
                                            toCorrectHtml(it)
                                        }
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

                manager.createProblemDescriptor(element, fix.toSelectionRange(), getProblemMessage(fix),
                        fix.info.category.highlight, isOnTheFly, *fixes.toTypedArray())
            }
        }

        override fun reset() {
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

                for (ext in LanguageSupport.allForLanguage(element.language).filter { it.isRelevant(element) }) {
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
