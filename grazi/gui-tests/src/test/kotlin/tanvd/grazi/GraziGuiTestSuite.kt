package tanvd.grazi

import com.intellij.testGuiFramework.framework.GuiTestSuite
import com.intellij.testGuiFramework.framework.GuiTestSuiteRunner
import org.junit.runner.RunWith
import org.junit.runners.Suite
import tanvd.grazi.ide.ui.LanguageListGuiTest
import tanvd.grazi.ide.ui.NativeLanguageGuiTest
import tanvd.grazi.ide.ui.RulesTreeGuiTest
import tanvd.grazi.ide.ui.SpellcheckGuiTest

@RunWith(GuiTestSuiteRunner::class)
@Suite.SuiteClasses(RulesTreeGuiTest::class, SpellcheckGuiTest::class)
class GraziGuiTestSuite : GuiTestSuite()
