package com.intellij.protobuf.python

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.protobuf.gencodeutils.GotoExpectationMarker
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.psi.PyExpression

class PbPythonGotoDeclarationTest : PbPythonTestBase() {

  fun testAll() = doTestGotoDeclaration("all_goto_declaration.py.test")

  fun testStarImport() = doTestGotoDeclaration("all_goto_declaration_star_import.py.test")

  private fun doTestGotoDeclaration(testFile: String) = runWithGeneratedPb("all.proto") { context ->
    configureUser(testFile, context)

    testExpectations(GotoExpectationMarker::parseExpectations) { expectation, lineNumber ->
      val expr = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset).parentOfType<PyExpression>()
                 ?: error("No Python expression found at line $lineNumber")

      val gotoTargets = GotoDeclarationAction.findAllTargetElements(
        myFixture.project,
        myFixture.editor,
        myFixture.caretOffset,
      )

      expectation.checkGotoTargets(expr.text, gotoTargets, lineNumber)
    }
  }
}
