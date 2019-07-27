package tanvd.grazi.ide.ui

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.*
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import org.junit.Test
import tanvd.grazi.GraziGuiTestBase
import kotlin.test.assertEquals


@RunWithIde(CommunityIde::class)
class LanguageListGuiTest : GraziGuiTestBase() {
    @Test
    fun `test language list`() {
        simpleProject {
            waitAMoment()

            settings {
                val tree = jTree("English (US)")
                val list = jList("English (US)")
                assertEquals(1, list.contents().size)

                actionButton("Add").click()

                with(popupMenu("Russian")) {
                    assertEquals(langs.filter { it != "English (US)" }, listItems())
                    clickItem("Russian")
                }

                list.requireSelectedItems("Russian")

                actionButton("Add").click()
                with(popupMenu("Chinese")) {
                    assertEquals(langs.filter { it != "English (US)" && it != "Russian" }, listItems())
                    clickItem("Chinese")
                }

                list.requireSelectedItems("Chinese")
                assertEquals(3, list.contents().size)

                assertEquals(0, list.item("Chinese").index())
                assertEquals(2, list.item("Russian").index())

                assert(tree.path("Chinese").hasPath())
                assert(tree.path("Russian").hasPath())
            }


            openTestFile()

            editor {
                waitAMoment()
                moveToLine(1)
                typeText("// Сдесь два ошибка.\n")
                typeText("// It are eror")

                waitAMoment()
                waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 4)
                requireHighlights(HighlightSeverity.INFORMATION, "Проверка орфографии с исправлениями",
                        "два ошибка &rarr; два ошибкиСклонение  «числительное + существительное»Incorrect:В коробке лежало три карандаш.Correct:В коробке лежало три карандаша.",
                        "are &rarr; is'it' + non-3rd person verbIncorrect:It only matter to me.Correct:It only matters to me.", "Typo: In word 'eror'")
            }

            settings {
                val tree = jTree("English (US)")
                val list = jList("English (US)")

                list.selectItems("Russian", "Chinese")
                actionButton("Remove").click()

                assertEquals(1, list.contents().size)
                assert(!tree.path("Chinese").hasPath())
                assert(!tree.path("Russian").hasPath())
            }

            editor {
                waitAMoment()
                waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 2)
                requireHighlights(HighlightSeverity.INFORMATION,
                        "are &rarr; is'it' + non-3rd person verbIncorrect:It only matter to me.Correct:It only matters to me.", "Typo: In word 'eror'")
            }
        }
    }

    @Test
    fun `test enabled-disabled actions in language list`() {
        settings("Cancel") {
            val add = actionButton("Add")
            val remove = actionButton("Remove")

            add.requireEnabled()
            remove.requireEnabled()

            val list = jList("English (US)")
            list.selectItem("English (US)")
            remove.click()

            waitAMoment()

            remove.requireDisabled()
        }
    }
}
