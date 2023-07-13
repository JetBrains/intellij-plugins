package org.jetbrains.vuejs.lang

import com.intellij.webSymbols.checkDocumentationAtCaret
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import one.util.streamex.StreamEx
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.util.*

@RunWith(com.intellij.testFramework.Parameterized::class)
class VueWebTypesDocumentationTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = TEST_DATA_PATH

  override fun getBasePath(): String = "/"

  @Before
  fun before() {
    myFixture.configureVueDependencies("test-lib" to "0.0.0")
    myFixture.copyDirectoryToProject("node_modules", "node_modules")
  }

  @Parameterized.Parameter
  @JvmField
  var myFileName: String? = null

  @Test
  fun testTypes() {
    defaultTest()
  }

  private fun defaultTest() {
    myFixture.configureByFile("${myFileName!!}.vue")
    myFixture.checkDocumentationAtCaret()
  }

  companion object {
    @JvmStatic
    val TEST_DATA_PATH = getVueTestDataPath() + "/documentation/web-types"

    @JvmStatic
    @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
    fun testNames(@Suppress("UNUSED_PARAMETER") klass: Class<*>): List<String> {
      val testData = File(TEST_DATA_PATH)
      return StreamEx.of(*testData.listFiles()!!)
        .filter { file -> file.isFile && file.name.endsWith(".vue") }
        .map { file -> file.name.substring(0, file.name.length - 4) }
        .toList()
    }

    @JvmStatic
    @Parameterized.Parameters
    fun data(): Collection<Any> {
      return ArrayList()
    }
  }

}
