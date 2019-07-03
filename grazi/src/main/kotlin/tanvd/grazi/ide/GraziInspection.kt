package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.ide.quickfix.GraziAddWord
import tanvd.grazi.ide.quickfix.GraziDisableRule
import tanvd.grazi.ide.quickfix.GraziRenameTypo
import tanvd.grazi.ide.quickfix.GraziReplaceTypo
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*
import tanvd.kex.buildList

class GraziInspection : LocalInspectionTool() {
    companion object {


        private fun getProblemMessage(fix: Typo): String {
            if (GraziPlugin.isTest) return ""

            val message = if (fix.isSpellingTypo) {
                //language=HTML
                """
                    <html>
                        <body>
                            <div>
                                <p>${fix.info.rule.toDescriptionSanitized()}</p>
                            </div>
                        </body>
                    </html>
                """.trimIndent()
            } else {
                val examples = fix.info.incorrectExample?.let {
                    val corrections = it.corrections.filter { it?.isNotBlank() ?: false }
                    if (corrections.isEmpty()) {
                        //language=HTML
                        """
                            <tr style='padding-top: 5px;'>
                                <td valign='top' style='color: gray;'>Incorrect:</td>
                                <td>${it.toIncorrectHtml()}</td>
                            </tr>
                        """.trimIndent()

                    } else {
                        //language=HTML
                        """
                            <tr style='padding-top: 5px;'>
                                <td valign='top'  style='color: gray;'>Incorrect:</td>
                                <td style='text-align: left'>${it.toIncorrectHtml()}</td>
                            </tr>
                            <tr>
                                <td valign='top'  style='color: gray;'>Correct:</td>
                                <td style='text-align: left'>${it.toCorrectHtml()}</td>
                            </tr>
                        """.trimIndent()
                    }
                } ?: ""

                val fixes = if (fix.fixes.isNotEmpty()) {
                    //language=HTML
                    """
                        <tr><td colspan='2' style='padding-bottom: 3px;'>${fix.word} &rarr; ${fix.fixes.take(3).joinToString(separator = "/")}</td></tr>
                    """
                } else ""

                //language=HTML
                """
                    <html>
                        <body>
                            <div>
                                <table>
                                $fixes
                                <tr><td colspan='2'>${fix.info.rule.toDescriptionSanitized()}</td></tr>
                                </table>
                                <table>
                                $examples
                                </table>
                            </div>
                        </body>
                    </html>
                """.trimIndent()
            }
            if (fix.info.rule.description.length > 50 || (!fix.isSpellingTypo && fix.info.incorrectExample?.example?.length ?: 0 > 50)) {
                return message.replaceFirst("<div>", "<div style='width: 300px;'>")
            }
            return message.filterOutNewLines()
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

                for (ext in LanguageSupport.all.filter { it.isSupported(element.language) && it.isRelevant(element) }) {
                    typos.addAll(ext.getFixes(element))
                }

                if (GraziConfig.state.enabledSpellcheck) {
                    typos.addAll(GraziSpellchecker.getFixes(element))
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
