package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSAbstractDocumentationTest
import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.runInEdtAndWait
import one.util.streamex.StreamEx
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.util.*

@RunWith(com.intellij.testFramework.Parameterized::class)
class VueWebTypesDocumentationTest : JSAbstractDocumentationTest() {

  override fun getTestDataPath(): String = TEST_DATA_PATH

  override fun getBasePath(): String = "/"

  override fun getExtension(): String = "vue"

  @Before
  fun before() {
    createPackageJsonWithVueDependency(myFixture, """"test-lib":"0.0.0"""")
    myFixture.copyDirectoryToProject("node_modules", "node_modules")
  }

  @Parameterized.Parameter
  @JvmField
  var myFileName: String? = null

  override fun getTestName(lowercaseFirstLetter: Boolean): String {
    return myFileName!!
  }

  @Test
  fun testTypes() {
    defaultTest()
  }

  companion object {
    @JvmStatic
    val TEST_DATA_PATH = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/documentation/web-types"

    @JvmStatic
    @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
    fun testNames(@Suppress("UNUSED_PARAMETER") klass: Class<*>): List<String> {
      val testData = File(TEST_DATA_PATH)
      return StreamEx.of<File>(*testData.listFiles()!!)
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
