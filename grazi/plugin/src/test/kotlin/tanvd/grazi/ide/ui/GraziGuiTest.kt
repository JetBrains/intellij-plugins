package tanvd.grazi.ide.ui

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.*
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import org.junit.Test
import kotlin.test.assertEquals


@RunWithIde(CommunityIde::class)
class GraziGuiTest : GuiTestCase() {
    @Test
    fun `test native language combobox`() {
        simpleProject {
            waitAMoment()
            openIdeSettings()
            settingsDialog {
                jTree("Tools", "Grazi").clickPath()
                with(combobox("Native language:")) {
                    val lang = "English (US)"
                    assertEquals(lang, selectedItem())
                    assertEquals(langs, listItems())

                    with(jTree(lang)) {
                        assert(path(lang, "False friends").hasPath())
                        assert(!path(lang, "Омонимы").hasPath())
                    }

                    selectItem("Russian")
                    button("Apply").clickWhenEnabled()

                    with(jTree(lang)) {
                        assert(!path(lang, "False friends").hasPath())
                        assert(path(lang, "Омонимы").hasPath())
                    }

                    selectItem(lang)
                }

                button("OK").click()
            }
        }
    }

    @Test
    fun `test spellcheck checkbox`() {
        simpleProject {
            waitAMoment()

            ideFrame {
                projectView {
                    path("SimpleProject", "src", "Main.kt").doubleClick()
                }

                editor {
                    moveToLine(1)
                    typeText("// text with eror")
                    waitAMoment()

                    requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
                }
            }

            openIdeSettings()
            settingsDialog {
                jTree("Tools", "Grazi").clickPath()

                with(checkbox("Enable Grazi spellchecker")) {
                    assert(!isSelected)
                    click()
                }

                button("OK").click()
            }

            ideFrame {
                editor {
                    waitAMoment()
                    requireHighlights(HighlightSeverity.INFORMATION, "Possible spelling mistake")
                }
            }

            ideFrame {
                invokeMainMenu("Start.Use.Vcs")
                dialog("Enable Version Control Integration") {
                    combobox("Select a version control system to associate with the project root:").selectItem("Git")
                    button("OK").click()
                }

                invokeMainMenu("CheckinProject")
                dialog("Commit Changes") {
                    editor {
                        moveToLine(1)
                        typeText("text with eror")
                        waitAMoment()

                        requireHighlights(HighlightSeverity.INFORMATION, "Possible spelling mistake")
                    }

                    button("Cancel").click()
                }
            }

            openIdeSettings()
            settingsDialog {
                jTree("Tools", "Grazi").clickPath()

                with(checkbox("Enable Grazi spellchecker")) {
                    assert(isSelected)
                    click()
                }

                button("OK").click()
            }

            waitAMoment()
            ideFrame {
                invokeMainMenu("CheckinProject")
                dialog("Commit Changes") {
                    editor {
                        requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
                    }

                    button("Cancel").click()
                }
            }
        }
    }

    @Test
    fun `test language list`() {
        simpleProject {
            waitAMoment()
            openIdeSettings()
            settingsDialog {
                jTree("Tools", "Grazi").clickPath()

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

                button("OK").click()
            }

            ideFrame {
                projectView {
                    path("SimpleProject", "src", "Main.kt").doubleClick()
                }

                editor {
                    moveToLine(1)
                    typeText("// Сдесь два ошибка.\n")
                    typeText("// It are eror")
                    waitAMoment()

                    requireHighlights(HighlightSeverity.INFORMATION, "Проверка орфографии с исправлениями",
                            "два ошибка &rarr; два ошибкиСклонение  «числительное + существительное»Incorrect:В коробке лежало три карандаш.Correct:В коробке лежало три карандаша.",
                            "are &rarr; is'it' + non-3rd person verbIncorrect:It only matter to me.Correct:It only matters to me.", "Typo: In word 'eror'")
                }
            }

            openIdeSettings()
            settingsDialog {
                jTree("Tools", "Grazi").clickPath()

                val tree = jTree("English (US)")
                val list = jList("English (US)")

                list.selectItems("Russian", "Chinese")
                actionButton("Remove").click()

                assertEquals(1, list.contents().size)
                assert(!tree.path("Chinese").hasPath())
                assert(!tree.path("Russian").hasPath())

                button("OK").click()
            }

            waitAMoment()
            ideFrame {
                editor {
                    requireHighlights(HighlightSeverity.INFORMATION,
                            "are &rarr; is'it' + non-3rd person verbIncorrect:It only matter to me.Correct:It only matters to me.", "Typo: In word 'eror'")
                }
            }
        }

    }

    private val langs = listOf("Chinese", "Dutch", "English (Canadian)", "English (GB)", "English (US)",
            "French", "German (Austria)", "German (Germany)", "Greek", "Italian", "Japanese", "Persian", "Polish",
            "Portuguese (Brazil)", "Portuguese (Portugal)", "Romanian", "Russian", "Slovak", "Spanish", "Ukrainian")

}
