package com.intellij.prettier.wsl

import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5wsl.wslFixture
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil
import com.intellij.wsl.remote.javascript.wslNodeInterpreterFixture
import org.junit.jupiter.api.Test

@TestApplication
class PrettierWSLTest {
  companion object {
    private val wslFixture = wslFixture()
  }

  private val node by wslNodeInterpreterFixture(wslFixture)

  @Test
  fun testPrettierOnSave() {
    val fixture = node.fixture
    fixture.addFileToProject("package.json", "{\n" +
                                             "  \"version\": \"1.0.0\",\n" +
                                             "  \"devDependencies\": {\n" +
                                             "    \"prettier\": \"latest\"\n" +
                                             "  }\n" +
                                             "}\n")
    fixture.addFileToProject(".prettierrc", "{}")
    val prettierConfiguration = PrettierConfiguration.getInstance(fixture.project).state
    prettierConfiguration.configurationMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC
    prettierConfiguration.runOnSave = true
    node.runNpmInstall()
    fixture.configureByText("foo.js", "var  a=''")
    runInEdtAndWait {
      fixture.type(' ')
      fixture.performEditorAction("SaveAll")
      ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(fixture.project)
    }
    fixture.checkResult("var a = \"\";\n")
  }

  @Test
  fun testPrettierIgnoreOnSave() {
    val fixture = node.fixture
    fixture.addFileToProject("package.json", "{\n" +
                                             "  \"version\": \"1.0.0\",\n" +
                                             "  \"devDependencies\": {\n" +
                                             "    \"prettier\": \"latest\"\n" +
                                             "  }\n" +
                                             "}\n")
    fixture.addFileToProject(".prettierrc", "{}")
    val ignoreFile = fixture.addFileToProject(".customignore", "foo.js")
    val prettierConfiguration = PrettierConfiguration.getInstance(fixture.project).state
    prettierConfiguration.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL
    prettierConfiguration.customIgnorePath = VfsUtilCore.virtualToIoFile(ignoreFile.virtualFile).absolutePath
    prettierConfiguration.runOnSave = true
    node.runNpmInstall()
    fixture.configureByText("foo.js", "var  a=''")
    runInEdtAndWait {
      fixture.type(' ')
      fixture.performEditorAction("SaveAll")
      ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(fixture.project)
    }
    fixture.checkResult(" var  a=''")
  }
}