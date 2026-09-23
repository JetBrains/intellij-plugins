// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.application.options.CodeStyle
import com.intellij.configurationStore.deserializeAndLoadState
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.ide.macro.Macro
import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.TrustedProjectsTestUtil
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.common.runAll
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.TempDirTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.actions.TfExternalToolsAction
import org.intellij.terraform.config.inspection.HclBlockMissingPropertyInspection
import org.intellij.terraform.config.model.local.TERRAFORM_LOCK_FILE_NAME
import org.intellij.terraform.config.model.local.TfLocalSchemaService
import org.intellij.terraform.config.util.TfCommandLineServiceMock
import org.intellij.terraform.hcl.formatter.TfAsyncFormattingService
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.macros.TfExecutableMacro
import org.intellij.terraform.runtime.TfProjectSettings
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Covers the trust gates that keep the Terraform tool path from being executed before the user trusts the project (IJPL-254867).
 *
 * The tests load the tool path from the `.idea/terraform.xml` state after the project becomes untrusted.
 * They do not cover the load order of the component store.
 */
internal class TfSafeModeTest : BasePlatformTestCase() {
  private lateinit var toolPath: Path
  private var moduleDir: VirtualFile? = null

  override fun runInDispatchThread(): Boolean = false

  override fun createTempDirTestFixture(): TempDirTestFixture = TempDirTestFixtureImpl()

  override fun setUp() {
    super.setUp()
    toolPath = Files.createTempFile("terraform-test", null)
    File(toolPath.toString()).setExecutable(true)
    TrustedProjectsTestUtil.enableTrustedProjectsCheck(testRootDisposable)
    TrustedProjects.setProjectTrusted(project, false)
    loadToolPathFromProjectXml(executablePath())
    TfCommandLineServiceMock.instance.clear()
  }

  override fun tearDown() {
    runAll(
      { timeoutRunBlocking { tfLocalSchemaService.awaitModelsReady() } },
      { TfCommandLineServiceMock.instance.throwErrorsIfAny() },
      { moduleDir?.let { VfsTestUtil.deleteFile(it) } },
      { TrustedProjects.setProjectTrusted(project, true) },
      { if (::toolPath.isInitialized) Files.deleteIfExists(toolPath) },
      { super.tearDown() },
    )
  }

  fun testFormatActionDoesNotRunInUntrustedProject() = timeoutRunBlocking {
    configureFormatAction()

    invokeFormatAction()
    TfExternalToolsAction.awaitTfExternalToolsActions(project)

    assertEmpty(TfCommandLineServiceMock.instance.requestsToVerify())
  }

  fun testFormatActionRunsInTrustedProject() = timeoutRunBlocking {
    val command = configureFormatAction()
    TrustedProjects.setProjectTrusted(project, true)

    invokeFormatAction()
    TfExternalToolsAction.awaitTfExternalToolsActions(project)

    assertEquals(listOf(command), TfCommandLineServiceMock.instance.requestsToVerify())
  }

  fun testLocalSchemaIsNotBuiltOnTfFileOpenInUntrustedProject() {
    val mainFile = configureTfModule()

    openAndHighlight(mainFile)
    timeoutRunBlocking { tfLocalSchemaService.awaitModelsReady() }

    assertNull(runReadAction { tfLocalSchemaService.getModel(mainFile) })
    assertEmpty(TfCommandLineServiceMock.instance.requestsToVerify())
  }

  fun testLocalSchemaIsBuiltOnTfFileOpenInTrustedProject() {
    val mainFile = configureTfModule()
    val command = "${executablePath()} providers schema -json"
    TfCommandLineServiceMock.instance.mockCommandLine(command, EMPTY_PROVIDER_SCHEMA, testRootDisposable)
    TrustedProjects.setProjectTrusted(project, true)

    openAndHighlight(mainFile)
    timeoutRunBlocking { tfLocalSchemaService.awaitModelsReady() }

    assertEquals(listOf(command), TfCommandLineServiceMock.instance.requestsToVerify())
    assertNotNull(runReadAction { tfLocalSchemaService.getModel(mainFile) })
  }

  fun testExecutableMacroCancelsExecutionInUntrustedProject() {
    // Global File Watchers run in Safe Mode projects, so the macro they expand must not start a process.
    assertThrows(Macro.ExecutionCancelledException::class.java) { expandExecutableMacro() }
  }

  fun testExecutableMacroExpandsToolPathInTrustedProject() {
    TrustedProjects.setProjectTrusted(project, true)

    assertEquals(executablePath(), expandExecutableMacro())
  }

  fun testExecutableMacroExpandsBinaryNameWhenToolPathIsEmpty() {
    loadToolPathFromProjectXml("")
    TrustedProjects.setProjectTrusted(project, true)

    assertEquals(TfToolType.TERRAFORM.getBinaryName(), expandExecutableMacro())
  }

  fun testFormattingTaskDoesNotRunInUntrustedProject() {
    val request = runFormattingTask()

    // Only the trust gate ends the task this way: the tool itself either reports formatted text or an error.
    assertTrue("the formatting task must finish without touching the document", request.isTextReady)
    assertNull(request.updatedText)
    assertNull(request.error)
  }

  private fun configureFormatAction(): String {
    val file = myFixture.tempDirFixture.createFile("main.tf", "resource \"local_file\" \"test\" {}")
    myFixture.configureFromExistingVirtualFile(file)
    val command = "${executablePath()} fmt ${file.path}"
    TfCommandLineServiceMock.instance.mockCommandLine(command, "", testRootDisposable)
    return command
  }

  private suspend fun invokeFormatAction() {
    val action = ActionUtil.getAction("TfFmtFileAction") ?: throw AssertionError("TfFmtFileAction is not registered")
    withContext(Dispatchers.EDT) {
      writeIntentReadAction {
        myFixture.testAction(action)
      }
    }
  }

  /**
   * Lays out the module from the report: a lock file that makes the plugin want a local schema, next to the file the victim opens.
   *
   * The module is under the source root, because inspections do not run on files outside the project content.
   */
  private fun configureTfModule(): VirtualFile {
    val root = ModuleRootManager.getInstance(module).sourceRoots.first()
    val dir = VfsTestUtil.createDir(root, "module")
    moduleDir = dir
    VfsTestUtil.createFile(dir, TERRAFORM_LOCK_FILE_NAME, LOCK_FILE_TEXT)
    return VfsTestUtil.createFile(dir, "main.tf", MAIN_TF_TEXT)
  }

  private fun openAndHighlight(file: VirtualFile) {
    myFixture.enableInspections(HclBlockMissingPropertyInspection::class.java)
    myFixture.configureFromExistingVirtualFile(file)
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
    myFixture.doHighlighting()
  }

  private fun expandExecutableMacro(): String? {
    val macro = Macro.EP_NAME.extensionList.firstOrNull { it.name == TfExecutableMacro.NAME }
                ?: throw AssertionError("${TfExecutableMacro.NAME} macro is not registered")
    return macro.expand(SimpleDataContext.getProjectContext(project))
  }

  /**
   * [TfAsyncFormattingService.canFormat] returns `false` in unit test mode, so the platform never reaches the task through a real
   * Reformat Code invocation. Drive the task directly instead, which is the only way to assert on the trust gate inside it.
   *
   * There is no trusted counterpart: `TfToolPathDetectorMock` always reports the tool as configured, so a trusted run would spawn the
   * real process.
   */
  private fun runFormattingTask(): RecordingFormattingRequest {
    val file = myFixture.tempDirFixture.createFile("main.tf", MAIN_TF_TEXT)
    myFixture.configureFromExistingVirtualFile(file)
    val service = FormattingService.EP_NAME.findExtensionOrFail(TfAsyncFormattingService::class.java)
    val request = runReadAction { RecordingFormattingRequest(myFixture.file) }
    val task = runReadAction { AsyncDocumentFormattingService.createFormattingTask(service, request) }
               ?: throw AssertionError("no formatting task for ${file.name}")
    // The task declares isRunUnderProgress(), and its runBlockingCancellable needs a cancellable context.
    ProgressManager.getInstance().runProcess({ task.run() }, EmptyProgressIndicator())
    return request
  }

  private fun loadToolPathFromProjectXml(path: String) {
    val element = JDOMUtil.load("""
      <component name="TerraformProjectSettings">
        <option name="toolPath" value="${StringUtil.escapeXmlEntities(path)}" />
      </component>
    """.trimIndent())
    deserializeAndLoadState(TfProjectSettings.getInstance(project), element)
  }

  private val tfLocalSchemaService: TfLocalSchemaService
    get() = project.service<TfLocalSchemaService>()

  private fun executablePath(): String = toolPath.toString()
}

private class RecordingFormattingRequest(psiFile: PsiFile) : AsyncFormattingRequest {
  var isTextReady: Boolean = false
    private set
  var updatedText: String? = null
    private set
  var error: String? = null
    private set

  private val formattingContext = FormattingContext.create(psiFile, CodeStyle.getSettings(psiFile))
  private val text = psiFile.text

  override fun getDocumentText(): String = text
  override fun getIOFile(): File? = null
  override fun getFormattingRanges(): List<TextRange> = listOf(TextRange(0, text.length))
  override fun canChangeWhitespaceOnly(): Boolean = false
  override fun isQuickFormat(): Boolean = false
  override fun getContext(): FormattingContext = formattingContext

  override fun onTextReady(updatedText: String?) {
    isTextReady = true
    this.updatedText = updatedText
  }

  override fun onError(title: String, message: String): Unit = onError(title, message, -1)

  override fun onError(title: String, message: String, offset: Int) {
    error = "$title: $message"
  }
}

private const val EMPTY_PROVIDER_SCHEMA: String = """{"format_version":"1.0","provider_schemas":{}}"""

private val LOCK_FILE_TEXT: String = """
  # This file is maintained automatically by "terraform init".
  # Manual edits may be lost in future updates.

  provider "registry.terraform.io/digitalocean/digitalocean" {
    version     = "2.34.1"
    constraints = "~> 2.0"
    hashes = [
      "h1:5tfXRq80lhTUCYxAqcUGL8BjR3SSTk+ggiW20UvK+JA=",
    ]
  }
""".trimIndent()

private val MAIN_TF_TEXT: String = """
  terraform {
    required_providers {
      digitalocean = {
        source  = "digitalocean/digitalocean"
        version = "~> 2.0"
      }
    }
  }

  resource "digitalocean_droplet" "web" {
    name = "web"
  }
""".trimIndent()
