package com.intellij.lang.javascript.linter.eslint.service

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration
import com.intellij.lang.javascript.linter.eslint.EslintRequestData
import com.intellij.lang.javascript.linter.eslint.EslintState
import com.intellij.lang.javascript.linter.eslint.EslintUtil
import com.intellij.lang.javascript.service.JSLanguageServiceExecutor
import com.intellij.lang.javascript.service.JSLanguageServiceQueue
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceCommand
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files

class ESLintBasedLanguageServiceTest : BasePlatformTestCase() {
  fun testHighlightSuspendingExecutesCommandAndParsesResponse() = timeoutRunBlocking {
    val service = createService(createQueue(highlightResponse()))
    val requestData = createRequestData(createLocalFile(), "const value = 1", createConfigFile())

    val response = service.highlightSuspending(requestData, "--quiet")

    assertNull(response!!.globalError)
    assertFalse(response.isNoConfigFile)
    assertEquals(1, response.value.size)
    val error = response.value.single()
    assertEquals("Unexpected constant.", error.description)
    assertEquals("no-const", error.code)
    assertEquals("GetErrors", service.executedCommand!!.command)
  }

  fun testFixFileSuspendingExecutesCommandAndProcessesResponse() = timeoutRunBlocking {
    val service = createService(createQueue(fixFileResponse()))
    val requestData = createRequestData(createLocalFile(), "const value = 1", createConfigFile())

    val response = service.fixFileSuspending(requestData, null)

    assertNull(response!!.globalError)
    assertFalse(response.isNoConfigFile)
    assertEquals("let value = 1", response.value)
    assertEquals("FixErrors", service.executedCommand!!.command)
  }

  fun testHighlightSuspendingReturnsServiceCreationFailureResponse() = timeoutRunBlocking {
    val service = createService(null)
    val requestData = createRequestData(createLocalFile(), "const value = 1")

    val response = service.highlightSuspending(requestData, null)

    assertNotNull(response!!.globalError)
    assertFalse(response.isNoConfigFile)
  }

  fun testHighlightSuspendingReturnsNullForBlankContentWithoutStartingProcess() = timeoutRunBlocking {
    val service = createService()
    val requestData = createRequestData(createLocalFile(), " \n\t")

    assertNull(service.highlightSuspending(requestData, "--quiet"))
    assertNull(service.processIfCreated)
  }

  fun testFixFileSuspendingReturnsNullForNonLocalFileWithoutStartingProcess() = timeoutRunBlocking {
    val service = createService()
    val requestData = createRequestData(LightVirtualFile("test.js", "const value = 1"), "const value = 1")

    assertNull(service.fixFileSuspending(requestData, null))
    assertNull(service.processIfCreated)
  }

  fun testCompletableFutureApiKeepsEarlyNulls() {
    val service = createService()

    assertNull(service.highlight(createRequestData(createLocalFile(), " \n\t"), "--quiet"))
    assertNull(service.fixFile(createRequestData(LightVirtualFile("test.js", "const value = 1"), "const value = 1"), null))
    assertNull(service.processIfCreated)
  }

  private fun createService(queue: JSLanguageServiceQueue? = createQueue()): TestESLintBasedLanguageService {
    val nodePackageDirectory = myFixture.tempDirFixture.findOrCreateDir("node_modules/eslint")
    return TestESLintBasedLanguageService(project,
                                          NodePackage(nodePackageDirectory.path),
                                          myFixture.tempDirFixture.findOrCreateDir("."),
                                          queue)
      .also { Disposer.register(testRootDisposable, it) }
  }

  private fun createLocalFile(): VirtualFile = createLocalVirtualFile("src/test.js", "const value = 1")

  private fun createConfigFile(): VirtualFile = createLocalVirtualFile("eslint.config.mjs", "export default []")

  private fun createLocalVirtualFile(relativePath: String, content: String): VirtualFile {
    val root = Files.createTempDirectory("eslint-based-language-service-test")
    Disposer.register(testRootDisposable) { root.toFile().deleteRecursively() }
    val path = root.resolve(relativePath)
    Files.createDirectories(path.parent)
    Files.writeString(path, content)
    return checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path))
  }

  private fun createRequestData(file: VirtualFile, content: String, config: VirtualFile? = null): EslintRequestData =
    EslintRequestData(file, EslintUtil.FileKind.JavaScriptAndOther, content, config, emptyList(), null, false)

  private fun createQueue(response: String = highlightResponse()): TestQueue = TestQueue(JSLanguageServiceAnswer(response))

  private fun highlightResponse(): String =
    """
      {
        "body": {
          "results": [
            {
              "messages": [
                {
                  "message": "Unexpected constant.",
                  "ruleId": "no-const",
                  "line": 1,
                  "column": 7,
                  "endLine": 1,
                  "endColumn": 12,
                  "severity": 2
                }
              ]
            }
          ]
        }
      }
    """.trimIndent()

  private fun fixFileResponse(): String =
    """
      {
        "body": {
          "results": [
            {
              "output": "let value = 1"
            }
          ]
        }
      }
    """.trimIndent()
}

private class TestESLintBasedLanguageService(
  project: Project,
  nodePackage: NodePackage,
  workingDirectory: VirtualFile,
  private val queue: JSLanguageServiceQueue?,
) : ESLintBasedLanguageService<EslintState>(project, nodePackage, workingDirectory) {
  val executedCommand: JSLanguageServiceCommand?
    get() = (queue as? TestQueue)?.executedCommand

  override fun getConfigurationClass(): Class<EslintConfiguration> = EslintConfiguration::class.java

  override suspend fun createLanguageServiceQueue(): JSLanguageServiceQueue? = queue
}

private class TestQueue(private val answer: JSLanguageServiceAnswer) : JSLanguageServiceQueue {
  var executedCommand: JSLanguageServiceCommand? = null
  override val startErrorMessage: String? = null
  override val state: JSLanguageServiceExecutor.State = JSLanguageServiceExecutor.State.STARTED
  override val isValid: Boolean = true

  override suspend fun execute(command: JSLanguageServiceCommand): JSLanguageServiceQueue.CommandResult {
    executedCommand = command
    return TestCommandResult(command, answer)
  }

  override fun dispose() {}
}

private class TestCommandResult(
  override val command: JSLanguageServiceCommand,
  override val answer: JSLanguageServiceAnswer,
) : JSLanguageServiceQueue.CommandResult {
  override val payload: List<JSLanguageServiceObject> = emptyList()
  override val cancelCommand: (() -> Unit)? = null
}
