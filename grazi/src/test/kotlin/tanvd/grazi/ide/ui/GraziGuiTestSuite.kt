package tanvd.grazi.ide.ui

import com.intellij.testGuiFramework.framework.GuiTestSuite
import com.intellij.testGuiFramework.framework.GuiTestSuiteRunner
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(GuiTestSuiteRunner::class)
@Suite.SuiteClasses(RulesTreeGuiTest::class, LanguageListGuiTest::class, SpellcheckGuiTest::class, NativeLanguageGuiTest::class)
class GraziGuiTestSuite : GuiTestSuite()
