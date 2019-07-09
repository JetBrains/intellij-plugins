package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.ide.quickfix.*
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*
import tanvd.kex.buildList

class GraziInspection : LocalInspectionTool() {
    companion object {
        private fun getProblemMessage(fix: Typo): String {
            if (ApplicationManager.getApplication().isUnitTestMode) return fix.info.rule.id
            return createHTML(false).html {
                body {
                    div {
                        style = "margin-bottom: 5px;"
                        if (fix.isSpellingTypo) {
                            div {
                                if (fix.info.rule.description.length > 50) {
                                    style = "width: 300px;"
                                }

                                p { unsafe { +fix.info.rule.toDescriptionSanitized() } }
                            }
                        } else {
                            div {
                                if (fix.info.rule.description.length > 50 || fix.info.incorrectExample?.example?.length ?: 0 > 50) {
                                    style = "width: 300px;"
                                }

                                table {
                                    if (fix.fixes.isNotEmpty()) {
                                        tr {
                                            td {
                                                colSpan = "2"
                                                style = "padding-bottom: 3px;"
                                                unsafe { +"${fix.word} &rarr; ${fix.fixes.take(3).joinToString(separator = "/")}" }
                                            }
                                        }
                                    }

                                    tr {
                                        td {
                                            colSpan = "2"
                                            unsafe { +fix.info.rule.toDescriptionSanitized() }
                                        }
                                    }
                                }

                                table {
                                    fix.info.incorrectExample?.let {
                                        val corrections = it.corrections.filter { it?.isNotBlank() ?: false }
                                        if (corrections.isEmpty()) {
                                            tr {
                                                style = "padding-top: 5px;"
                                                td {
                                                    style = "color: gray;"
                                                    +"Incorrect:"
                                                }
                                                td { unsafe { +it.toIncorrectHtml() } }
                                            }
                                        } else {
                                            tr {
                                                style = "padding-top: 5px;"
                                                td {
                                                    style = "color: gray;"
                                                    +"Incorrect:"
                                                }
                                                td {
                                                    style = "text-align: left"
                                                    unsafe { +it.toIncorrectHtml() }
                                                }
                                            }

                                            tr {
                                                style = "padding-top: 5px;"
                                                td {
                                                    style = "color: gray;"
                                                    +"Correct:"
                                                }
                                                td {
                                                    style = "text-align: left"
                                                    unsafe { +it.toCorrectHtml() }
                                                }
                                            }
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
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement?) {
                element ?: return

                val typos = HashSet<Typo>()

                for (ext in LanguageSupport.allForLanguage(element.language).filter { it.isRelevant(element) }) {
                    typos.addAll(ext.getTypos(element))
                }

                if (GraziConfig.state.enabledSpellcheck) {
                    typos.addAll(GraziSpellchecker.getTypos(element))
                }

                typos.mapNotNull { createProblemDescriptor(it, holder.manager, isOnTheFly) }.forEach {
                    holder.registerProblem(it)
                }

                super.visitElement(element)
            }
        }
    }

    override fun getDisplayName(): String {
        return "Grazi proofreading inspection"
    }
}
