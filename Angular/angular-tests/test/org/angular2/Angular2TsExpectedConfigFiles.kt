package org.angular2

import com.intellij.javascript.testFramework.web.WebFrameworkTestConfigurator
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.openapi.Disposable
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import java.io.FileNotFoundException

/**
 * Ensures that only specified configs are used for type evaluation
 */
class Angular2TsExpectedConfigFiles(
  vararg paths: String,
) : WebFrameworkTestConfigurator {

  private val paths = paths.toList()

  override fun configure(fixture: CodeInsightTestFixture, disposable: Disposable?) {
    TypeScriptServerServiceImpl.requireTSConfigsForTypeEvaluation(
      fixture.testRootDisposable,
      *paths.map { path -> fixture.tempDirFixture.getFile(path) ?: throw FileNotFoundException(path) }
        .toList().toTypedArray()
    )
  }

}