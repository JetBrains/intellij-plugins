package org.angular2

import com.intellij.testFramework.LexerTestCase
import org.jetbrains.annotations.NonNls

abstract class AngularLexerTestCase: LexerTestCase() {

  override fun getPathToTestDataFile(extension: String): String =
    dirPath + "/" + getTestName(true) + extension

  override fun doTest(text: @NonNls String) {
    super.doTest(text)
    checkCorrectRestart(text)
  }

}