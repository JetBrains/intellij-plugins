package com.intellij.protobuf.python

import com.intellij.protobuf.gencodeutils.CompletionExpectationMarker
import com.intellij.psi.util.PsiUtilCore

class PbPythonCompletionTest : PbPythonTestBase() {

  fun testAll() = runWithGeneratedPb("all.proto") { context ->
    configureUser("all_completion.py.test", context)

    testExpectations(CompletionExpectationMarker::parseExpectations) { expectation, lineNumber ->
      val items = myFixture.completeBasic()
      val completions = if (items == null) {
        // The only completion is already auto-completed
        listOf(PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset - 1).text)
      }
      else {
        myFixture.lookupElementStrings.orEmpty()
      }

      expectation.checkCompletions(completions, lineNumber)
    }
  }
}
