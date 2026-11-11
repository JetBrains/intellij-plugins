// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult
import com.intellij.lang.javascript.linter.LinterHighlightingFixture
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import java.io.File
import java.lang.reflect.Method
import java.util.function.Supplier

/**
 * The linter fixture of the ESLint and StandardJS tests with a global install.
 * Create it with [eslintServiceFixture].
 * The setup points the ESLint service at the Node.js modules of the global install.
 */
class EslintServiceFixture(
  fixture: CodeInsightTestFixture,
  testClass: Class<*>,
  testMethod: Method,
  options: JSTempDirFixtureOptions,
  inspection: Supplier<out InspectionProfileEntry>,
  /** `true` reads the file-level annotation from the ESLint service. See [getEslintFileLevelAnnotationText]. */
  private val serviceFileLevelAnnotation: Boolean,
) : LinterHighlightingFixture(fixture, testClass, testMethod, options, inspection) {

  override fun setUp() {
    super.setUp()
    Registry.get("eslint.service.node.path").setValue(File(nodePackage.systemDependentPath).parent, testRootDisposable)
  }

  override fun getAnnotationText(): String? =
    if (serviceFileLevelAnnotation) getEslintFileLevelAnnotationText(project, fixture.file.virtualFile) else super.getAnnotationText()

  /**
   * Runs the ESLint analysis pipeline against a service whose request never completes, so the analysis times out.
   * Deterministically exercises the timeout handling without spawning a real node service process (WEB-67172).
   * Set the desired timeout (e.g. `JSLanguageServiceUtil.setTimeout(1, ...)`) before calling this.
   */
  fun highlightWithNeverRespondingService(psiFile: PsiFile): JSLinterAnnotationResult =
    highlightWithNeverRespondingService(project, psiFile, nodePackage)

  companion object {
    /**
     * Creates an [EslintServiceFixture] on a new project.
     * Set the main package of [options] to the package of the linter.
     * Declare the result as an instance property, so that each test gets its own project.
     */
    @JvmStatic
    @JvmOverloads
    fun eslintServiceFixture(options: JSTempDirFixtureOptions,
                             inspection: Supplier<out InspectionProfileEntry>,
                             serviceFileLevelAnnotation: Boolean = false): TestFixture<EslintServiceFixture> =
      jsTempDirFixture("eslintServiceFixture", options.withoutDefaultInspections()) { fixture, testClass, testMethod, o ->
        EslintServiceFixture(fixture, testClass, testMethod, o, inspection, serviceFileLevelAnnotation)
      }
  }
}
