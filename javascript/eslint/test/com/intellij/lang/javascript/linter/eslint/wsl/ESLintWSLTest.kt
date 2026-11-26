package com.intellij.lang.javascript.linter.eslint.wsl

import com.intellij.lang.javascript.linter.eslint.EslintInspection
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5wsl.wslFixture
import com.intellij.wsl.remote.javascript.wslNodeInterpreterFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class ESLintWSLTest {
  companion object {
    private val wslFixture = wslFixture()
  }

  private val node by wslNodeInterpreterFixture(wslFixture)

  @BeforeEach
  fun setUp() {
    node.fixture.enableInspections(EslintInspection())
  }

  @Test
  fun testEslintBasicHighlighting() {
    val fixture = node.fixture
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
    node.runNpmInstall()
    fixture.testHighlighting()
  }

  @Test
  fun testEslintFixFile() {
    val fixture = node.fixture
    fixture.addFileToProject("package.json", "{\n" +
                                             "  \"version\": \"1.0.0\",\n" +
                                             "  \"devDependencies\": {\n" +
                                             "    \"eslint\": \"latest\"\n" +
                                             "  }\n" +
                                             "}\n")
    fixture.addFileToProject("eslint.config.js", "module.exports = {rules: {\"semi\": \"error\"}}")
    fixture.configureByText("foo.js", "let a = ''")
    node.runNpmInstall()
    fixture.launchAction(fixture.getAvailableIntention("ESLint: Fix current file")!!)
    fixture.checkResult("let a = '';")
  }
}
