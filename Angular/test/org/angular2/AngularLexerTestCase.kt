package org.angular2

import com.intellij.testFramework.LexerTestCase

abstract class AngularLexerTestCase: LexerTestCase() {

  override fun getPathToTestDataFile(extension: String): String =
    dirPath + "/" + getTestName(true) + extension

}