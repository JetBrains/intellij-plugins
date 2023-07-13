// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.intellij.openapi.application.ReadAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import one.util.streamex.StreamEx
import org.angular2.cli.config.AngularConfig
import org.angular2.cli.config.AngularConfigProvider.Companion.getAngularConfig
import org.angularjs.AngularTestUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@RunWith(com.intellij.testFramework.Parameterized::class)
class Angular2ConfigTest : BasePlatformTestCase() {
  @Parameterized.Parameter
  @JvmField
  var myDirName: String? = null

  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "cli/config"
  }

  @Test
  fun testParsing() {
    val vFile = myFixture.copyDirectoryToProject(myDirName!!, "./")
    val config = ReadAction.compute<AngularConfig?, RuntimeException> {
      getAngularConfig(
        project, vFile)
    }!!
    myFixture.configureByText("out.txt", config.toString() + "\n")
    myFixture.checkResultByFile(myDirName + "/" + config.angularJsonFile.getName() + ".parsed",
                                true)
  }

  @Test
  @Throws(Exception::class)
  fun testTsLintConfigSelection() {
    val rootDir = myFixture.copyDirectoryToProject(myDirName!!, "./")
    val config = ReadAction.compute<AngularConfig, RuntimeException> { getAngularConfig(project, rootDir) }
    TestCase.assertNotNull(config)
    val tslintTest = rootDir.findFileByRelativePath("tslint-test.json") ?: error("no tslint-test.json")
    var tests: JsonObject
    tslintTest.getInputStream().use { `in` -> tests = JsonParser().parse(InputStreamReader(`in`, StandardCharsets.UTF_8)) as JsonObject }
    for (entry in tests.entrySet()) {
      val file = myFixture.findFileInTempDir(entry.key!!) ?: error(entry.key!!)
      val value = config.getProject(file)
        ?.tsLintConfigurations
        ?.firstNotNullOfOrNull { it.getTsLintConfig(file) }
        ?.path
      if (value != null) {
        entry.setValue(JsonPrimitive(value))
      }
      else {
        entry.setValue(null)
      }
    }
    myFixture.configureByText("out.txt",
                              GsonBuilder().setPrettyPrinting()
                                .serializeNulls()
                                .create()
                                .toJson(tests) + "\n")
    myFixture.checkResultByFile("$myDirName/tslint-test.json",
                                true)
  }

  companion object {
    @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
    @Suppress("unused")
    @JvmStatic
    fun testNames(klass: Class<*>): List<String> {
      val testData = File(AngularTestUtil.getBaseTestDataPath() + "cli/config")
      return StreamEx.of(*testData.listFiles())
        .filter { file: File -> file.isDirectory() }
        .map { file: File -> file.getName() }
        .toList()
    }

    @Parameterized.Parameters
    @JvmStatic
    fun data(): Collection<Any> {
      return ArrayList()
    }
  }
}
