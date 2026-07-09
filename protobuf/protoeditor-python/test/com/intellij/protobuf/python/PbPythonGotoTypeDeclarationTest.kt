package com.intellij.protobuf.python

import com.intellij.codeInsight.navigation.actions.GotoTypeDeclarationAction
import com.intellij.protobuf.gencodeutils.GotoExpectationMarker
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.psi.PyExpression

class PbPythonGotoTypeDeclarationTest : PbPythonTestBase() {

  fun testAll() = runWithGeneratedPb("all.proto") { context ->
    configureUser("all_goto_type.py.test", context)

    testExpectations(GotoExpectationMarker::parseExpectations) { expectation, lineNumber ->
      val expr = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset).parentOfType<PyExpression>()
                 ?: error("No Python expression found at line $lineNumber")

      val gotoTargets = GotoTypeDeclarationAction.findSymbolTypes(myFixture.editor, myFixture.caretOffset)
                          ?.filterNotNull()
                          ?.toTypedArray()
                        ?: PsiElement.EMPTY_ARRAY

      expectation.checkGotoTargets(expr.text, gotoTargets, lineNumber)
    }
  }
}
