package com.intellij.lang.javascript.linter.eslint.wsl

import com.intellij.lang.javascript.linter.eslint.EslintInspection
import com.intellij.wsl.WSLTempDirWithNodeInterpreterBase
import org.junit.Test

class ESLintWSLTest : WSLTempDirWithNodeInterpreterBase() {

  override fun setUp() {
    super.setUp()
    fixture.enableInspections(EslintInspection())
  }

  @Test
  fun testEslintBasicHighlighting() {
    fixture.addFileToProject("package.json", "{\n" +
                                             "  \"version\": \"1.0.0\",\n" +
                                             "  \"devDependencies\": {\n" +
                                             "    \"eslint\": \"latest\"\n" +
                                             "  }\n" +
                                             "}\n")
    fixture.addFileToProject("eslint.config.cjs", "module.exports = {\n" +
                                                  "  rules: {\n" +
                                                  "    \"no-console\": \"error\",\n" +
                                                  "    \"semi\": \"error\"\n" +
                                                  "  }\n" +
                                                  "}\n")
    fixture.configureByText("app.js", "<error descr=\"ESLint: Unexpected console statement. (no-console)\">console.log</error>('message'<error descr=\"ESLint: Missing semicolon. (semi)\">)</error>")
    runNpmInstall()
    fixture.testHighlighting()
  }

  @Test
  fun testEslintFixFile() {
    fixture.addFileToProject("package.json", "{\n" +
                                             "  \"version\": \"1.0.0\",\n" +
                                             "  \"devDependencies\": {\n" +
                                             "    \"eslint\": \"latest\"\n" +
                                             "  }\n" +
                                             "}\n")
    fixture.addFileToProject("eslint.config.js", "module.exports = {rules: {\"semi\": \"error\"}}")
    fixture.configureByText("foo.js", "let a = ''")
    runNpmInstall()
    fixture.launchAction(fixture.getAvailableIntention("ESLint: Fix current file")!!)
    fixture.checkResult("let a = '';")
  }
}