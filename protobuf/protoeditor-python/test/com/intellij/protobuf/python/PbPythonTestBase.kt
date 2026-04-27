package com.intellij.protobuf.python

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.protobuf.gencodeutils.ExpectationMarkerBase
import com.intellij.protobuf.ide.settings.BUNDLED_DESCRIPTOR
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.protobuf.python.PbPythonSourceContext.ApiVersion
import com.intellij.protobuf.python.PbPythonTestMocks.setupProtobufMocks
import com.intellij.psi.PsiFile
import com.jetbrains.python.fixtures.PyTestCase

abstract class PbPythonTestBase : PyTestCase() {

  internal data class GeneratedProtoContext(val apiVersion: ApiVersion, val baseName: String) {
    val importName: String = baseName + apiVersion.suffix
    val generatedPyFileName: String = "$importName.py"
    val generatedPyiFileName: String = "$importName.pyi"
  }

  override fun setUp() {
    super.setUp()
    PbProjectSettings.getInstance(myFixture.project).descriptorPath = BUNDLED_DESCRIPTOR
    setupProtobufMocks(myFixture)
  }

  override fun getTestDataPath(): String =
    PathManager.getHomeDir().toString() + "/contrib/protobuf/protoeditor-python/testData/"

  /**
   * Prepares generated Protobuf and runs [action] for available Protobuf API versions.
   *
   * State for [action]: Generated python code and .proto file are copied to the project.
   * Only one API version is present at a time.
   *
   * @param protoFile Name of the `.proto` file in [testData][getTestDataPath]`/proto/`
   * @param requirePyi Whether to run with the `.pyi` stub file only.
   * Otherwise, runs both with and without `.pyi` by default
   * @param action The test logic to execute. Receives [GeneratedProtoContext]
   */
  internal fun runWithGeneratedPb(
    protoFile: String,
    requirePyi: Boolean = false,
    action: (GeneratedProtoContext) -> Unit,
  ) {
    val baseName = protoFile.removeSuffix(".proto")
    val sourceOfGeneratedPyFile = "gen/${baseName}${ApiVersion.V2.suffix}.py"
    val sourceOfGeneratedPyiFile = "gen/${baseName}${ApiVersion.V2.suffix}.pyi"

    myFixture.copyDirectoryToProject("proto", "proto")

    for (apiVersion in ApiVersion.entries) {
      val context = GeneratedProtoContext(apiVersion, baseName)

      val withStubVariants = if (requirePyi) listOf(true) else listOf(false, true)

      for (withStub in withStubVariants) {
        myFixture.copyFileToProject(sourceOfGeneratedPyFile, context.generatedPyFileName)

        if (withStub) {
          myFixture.copyFileToProject(sourceOfGeneratedPyiFile, context.generatedPyiFileName)
        }

        try {
          action(context)
        }
        catch (e: Throwable) {
          val mode = if (withStub) "with .pyi" else "without .pyi"
          throw AssertionError("Failed with generated '${apiVersion.suffix}' file ($mode)", e)
        }
        finally {
          deleteFileFromProject(context.generatedPyFileName)
          if (withStub) {
            deleteFileFromProject(context.generatedPyiFileName)
          }
        }
      }
    }
  }

  /**
   * Loads test file.
   *
   * State after: [testFile] is pre-configured in [myFixture].
   *
   * @param testFile Name of the `.py.test` file in [testData][getTestDataPath]`/users/`.
   * `$importName` will be substituted with the generated `.py` file
   * based on the API version from [context].
   * @param context [GeneratedProtoContext] from [runWithGeneratedPb]
   */
  internal fun configureUser(
    testFile: String,
    context: GeneratedProtoContext,
  ) {
    val pythonTextRaw = VfsUtil.loadText(myFixture.copyFileToProject("users/$testFile"))

    val pythonText = pythonTextRaw
      .replace("${ApiVersion.V2.suffix}.py", "${context.apiVersion.suffix}.py")
      .replace($$"$importName", context.importName)

    myFixture.configureByText("test.py", pythonText)
  }

  /**
   * Runs [action] for matching pairs of expectation and caret.
   *
   * State before: Test file should be pre-configured in [myFixture].
   *
   * State for [action]: Test file with a single `<caret>`
   * at the current testing position is pre-configured in [myFixture].
   *
   * @param expectationParser Parser function that produces expectation markers.
   * See [ExpectationMarkerBase]
   * @param action The test logic to execute. Receives a single expectation marker
   * and a human-readable line number of the caret (e.g., for logging).
   * For debugging, a conditional breakpoint on line number can be set
   */
  // TODO: Ensure tests run all carets and not skip something silently (preferably without hardcoding the counter)
  protected fun <T : ExpectationMarkerBase> testExpectations(
    expectationParser: (PsiFile) -> List<T>,
    action: (T, Int) -> Unit,
  ) {
    val file = checkNotNull(myFixture.file) { "Test file is not configured in myFixture. Use configureUser(...)" }
    val expectations = expectationParser(file)
    val errors = mutableListOf<Throwable>()

    val cleanText = myFixture.file.text
    val caretOffsets = myFixture.editor.caretModel.allCarets.map { it.offset }
    myFixture.editor.caretModel.removeSecondaryCarets()

    var lastStamp = myFixture.editor.document.modificationStamp

    for (expectation in expectations) {
      for (caretOffset in caretOffsets.filter { expectation.textRange.contains(it) }) {
        if (myFixture.editor.document.modificationStamp != lastStamp) {
          myFixture.configureByText("test.py", cleanText)
          lastStamp = myFixture.editor.document.modificationStamp
        }

        val lineNumber = myFixture.editor.document.getLineNumber(caretOffset) + 1
        myFixture.editor.caretModel.moveToOffset(caretOffset)

        try {
          action(expectation, lineNumber)
        }
        catch (e: Throwable) {
          errors.add(e)
        }
      }
    }

    if (errors.isNotEmpty()) {
      val message = errors.joinToString("\n") { it.message ?: it.toString() }
      throw AssertionError(
        "Failed with ${errors.size} error(s). " +
        "You can set a breakpoint using the line number provided by the failure message below:\n" +
        message
      ).apply {
        errors.forEach { addSuppressed(it) }
      }
    }
  }

  protected fun deleteFileFromProject(path: String) {
    val file = myFixture.findFileInTempDir(path) ?: return
    WriteAction.runAndWait<Throwable> {
      file.delete(this)
    }
  }
}
