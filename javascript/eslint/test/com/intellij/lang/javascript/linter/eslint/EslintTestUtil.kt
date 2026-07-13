@file:JvmName("EslintTestUtil")

package com.intellij.lang.javascript.linter.eslint

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult
import com.intellij.lang.javascript.linter.JSLinterInput
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceClient
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import java.io.File
import java.util.concurrent.CompletableFuture

private const val ESLINT_TEST_DATA_PATH = "/javascript/eslint/testData"

@JvmField
val ESLINT_TEST_DATA_RELATIVE_PATH: String = "/contrib$ESLINT_TEST_DATA_PATH"

fun getEslintTestDataPath(): String =
  getContribPath() + ESLINT_TEST_DATA_PATH

fun getEslintTestDataRelativePath(): String = ESLINT_TEST_DATA_RELATIVE_PATH

private fun getContribPath(): String {
  val homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy()
  return if (File(homePath, "contrib/.gitignore").isFile) {
    homePath + File.separatorChar + "contrib"
  }
  else homePath
}

/**
 * Runs the ESLint analysis pipeline against a fake service whose request never completes, so the
 * analysis times out deterministically without spawning a real node process (WEB-67172 rationale).
 * Set the timeout before calling (e.g. `JSLanguageServiceUtil.setTimeout(1L, ...)`).
 *
 * Shared by [EslintServiceTestBase] (global-install tiers) and [EslintPackageLockTestBase]
 * (pinned-lock tiers), which previously each carried an identical copy of the fake service.
 */
fun highlightWithNeverRespondingService(project: Project, psiFile: PsiFile, nodePackage: NodePackage): JSLinterAnnotationResult {
  val service = createNeverRespondingEslintService(nodePackage, psiFile.virtualFile.parent)
  val state = EslintConfiguration.getInstance(project).extendedState.state
  val input = JSLinterInput.create(psiFile, state, null)
  return EsLintExternalRunner.highlight(input, service, true)
}

private fun createNeverRespondingEslintService(nodePackage: NodePackage, workingDirectory: VirtualFile): EslintLanguageServiceClient =
  object : EslintLanguageServiceClient {
    override fun getNodePackage(): NodePackage = nodePackage
    override fun getWorkingDirectory(): VirtualFile = workingDirectory
    override fun highlight(requestData: EslintRequestData, extraOptions: String?)
      : CompletableFuture<EslintLanguageServiceClient.Response<List<EslintError>>> = CompletableFuture() // never completes -> times out

    override fun fixFile(requestData: EslintRequestData, extraOptions: String?)
      : CompletableFuture<EslintLanguageServiceClient.Response<String>> = CompletableFuture()

    override fun isServiceCreated(): Boolean = true
    override fun getServiceCreationError(): String? = null
  }

/**
 * The file-level ESLint annotation text, read through [EslintLanguageServiceManager] rather than the
 * `JSLinterEditorNotifications` channel that [com.intellij.lang.javascript.linter.LinterHighlightingTest.getAnnotationText]
 * uses (see `ESLintAnnotationsBuilder.applyFileLevelAnnotation`; WEB-67129 lineage). Shared by the
 * tearDown global-annotation check in [EslintPackageLockTestBase] and [EslintYarnPnpTest].
 */
fun getEslintFileLevelAnnotationText(project: Project, file: VirtualFile): String? {
  val state = EslintConfiguration.getInstance(project).extendedState.state
  return EslintLanguageServiceManager.getInstance(project)
    .useService<String?, RuntimeException>(file, state.nodePackageRef) { service ->
      service?.fileLevelAnnotation?.message
    }
}
