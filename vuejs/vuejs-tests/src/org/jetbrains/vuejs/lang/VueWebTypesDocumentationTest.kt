package org.jetbrains.vuejs.lang

import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.checkDocumentationAtCaret
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueWebTypesDocumentationTest {
  private val hybrid by hybridFixture(TEST_DATA_PATH)
  private val myFixture get() = hybrid.fixture

  @BeforeEach
  fun before() {
    myFixture.configureVueDependencies("test-lib" to "0.0.0")
    myFixture.copyDirectoryToProject("node_modules", "node_modules")
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testNames")
  fun testTypes(myFileName: String): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    defaultTest(myFileName)
  }

  private fun defaultTest(myFileName: String) {
    myFixture.configureByFile("${myFileName}.vue")
    myFixture.checkDocumentationAtCaret()
  }

  companion object {
    @JvmStatic
    val TEST_DATA_PATH = getVueTestDataPath() + "/documentation/web-types"

    @JvmStatic
    fun testNames(): List<String> =
      File(TEST_DATA_PATH).listFiles()!!
        .filter { file -> file.isFile && file.name.endsWith(".vue") }
        .map { file -> file.name.substring(0, file.name.length - 4) }
  }

}
