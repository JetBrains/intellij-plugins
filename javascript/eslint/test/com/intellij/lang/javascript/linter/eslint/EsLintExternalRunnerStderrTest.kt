package com.intellij.lang.javascript.linter.eslint

import com.intellij.util.AnsiCsiUtil
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The IDE reports the stderr output of a lint only when it holds a logged exception (WEB-78300).
 */
class EsLintExternalRunnerStderrTest {
  @Test
  fun testLoggedExceptionHasStackFrame() {
    val loggedError = "Could not read file [object Object]\n" +
                      "Error: ENOENT: no such file or directory, open '/project/Helpers.mts.mts'\n" +
                      "    at readFileSync (node:fs:483:20)\n" +
                      "    at resolveImportedId (file:///project/node_modules/plugin/dist/resolve-imported-id.js:23:19) {\n" +
                      "  errno: -2,\n" +
                      "  code: 'ENOENT'\n" +
                      "}"
    assertTrue(EsLintExternalRunner.containsStackTrace(loggedError))
  }

  @Test
  fun testColoredStackFrameAfterTheColorCodesAreStripped() {
    // Node.js prints a stack with color codes when the IDE starts it with FORCE_COLOR. ESLint 10.13 has a function name
    // in every frame, so every line ends with a color code and no line ends with the position.
    val coloredFrame = "Error: ENOENT: no such file or directory\n" +
                       "    at Program \u001B[90m(file:///project/\u001B[39meslint.config.mjs?mtime=1793005936727:7:23\u001B[90m)\u001B[39m\n" +
                       "    at runRules \u001B[90m(/project/node_modules/\u001B[4meslint\u001B[24m/lib/linter/linter.js:719:12\u001B[90m)\u001B[39m"
    assertTrue(EsLintExternalRunner.containsStackTrace(AnsiCsiUtil.stripAnsi(coloredFrame)))
  }

  @Test
  fun testBunStackFrameWithoutFunctionName() {
    assertTrue(EsLintExternalRunner.containsStackTrace("error: boom\n      at /project/node_modules/plugin/index.js:10:5\n"))
  }

  @Test
  fun testWindowsPathStackFrame() {
    assertTrue(EsLintExternalRunner.containsStackTrace(
      "Error: boom\n" +
      "    at Program (C:\\Users\\dev\\project\\eslint.config.mjs:7:23)\n" +
      "    at C:\\Users\\dev\\project\\node_modules\\eslint\\lib\\linter\\linter.js:719:12"))
  }

  @Test
  fun testPlainWarningHasNoStackFrame() {
    assertFalse(EsLintExternalRunner.containsStackTrace(
      "Warning: React version not specified in eslint-plugin-react settings. " +
      "See https://github.com/jsx-eslint/eslint-plugin-react#configuration ."))
    assertFalse(EsLintExternalRunner.containsStackTrace("Could not read file [object Object]"))
    assertFalse(EsLintExternalRunner.containsStackTrace(null))
  }
}
