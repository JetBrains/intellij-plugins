package com.intellij.prettier.wsl

import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.wsl.WSLTempDirWithNodeInterpreterBase
import org.junit.Test
import java.awt.EventQueue

class PrettierWSLTest : WSLTempDirWithNodeInterpreterBase() {

  @Test
  fun testPrettierOnSave() {
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
    runNpmInstall()
    fixture.configureByText("foo.js", "var  a=''")
    fixture.type(' ')
    fixture.performEditorAction("SaveAll")
    EventQueue.invokeAndWait { PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue() }
    fixture.checkResult("var a = \"\";\n")
  }

  @Test
  fun testPrettierIgnoreOnSave() {
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
    runNpmInstall()
    fixture.configureByText("foo.js", "var  a=''")
    fixture.type(' ')
    fixture.performEditorAction("SaveAll")
    EventQueue.invokeAndWait { PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue() }
    fixture.checkResult("var  a='' ")
  }
}