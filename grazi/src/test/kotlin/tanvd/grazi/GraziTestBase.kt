package tanvd.grazi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainText
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import tanvd.grazi.ide.GraziInspection
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.filterFor
import java.io.File

abstract class GraziTestBase(private val withSpellcheck: Boolean) : LightCodeInsightFixtureTestCase() {

    override fun getTestDataPath(): String {
        return File("src/test/resources").canonicalPath
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(*inspectionTools)

        GraziConfig.update { it.copy(enabledSpellcheck = withSpellcheck) }

        while (ApplicationManager.getApplication().messageBus.hasUndeliveredEvents(GraziStateLifecycle.topic)) {
            Thread.sleep(500)
        }

        GraziConfig.update { it.copy(enabledLanguages = it.enabledLanguages + Lang.RUSSIAN) }
    }

    override fun tearDown() {
        super.tearDown()
        GraziConfig.update { it.copy(enabledLanguages = it.enabledLanguages - Lang.RUSSIAN) }
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return object : ProjectDescriptor(LanguageLevel.JDK_1_8) {
            override fun getSdk(): Sdk? {
                return JavaSdk.getInstance().createJdk("jdk8", System.getenv("JAVA_HOME"), false)
            }
        }
    }

    protected fun runHighlightTestForFile(file: String) {
        myFixture.configureByFile(file)
        myFixture.testHighlighting(true, false, false, file)
    }

    fun plain(vararg texts: String) = plain(texts.toList())

    fun plain(texts: List<String>): Collection<PsiElement> {
        return texts.flatMap { myFixture.configureByText("${it.hashCode()}.txt", it).filterFor<PsiPlainText>() }
    }


    companion object {
        private val inspectionTools by lazy { arrayOf(GraziInspection()) }
    }
}
