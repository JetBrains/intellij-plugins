// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.rename.HeadlessRenameProcessor
import com.intellij.refactoring.rename.HeadlessRenamePsiElementProcessor
import com.intellij.refactoring.rename.HeadlessRenameResult
import org.angular2.Angular2TestCase
import org.angular2.TestNoService
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.refactoring.Angular2PipeRenameProcessor
import org.junit.Test

@TestNoService
class Angular2HeadlessRenameTest : Angular2TestCase("refactoring/rename") {

  @Test
  fun testPipeRenamesItsClassAndFiles() =
    doConfiguredTest(dir = true, dirName = "pipeFromTS2NoStrings", checkResult = true, configureFileName = "foo.pipe.ts") {
      val pipeClass = PsiTreeUtil.findChildOfType(file, TypeScriptClass::class.java)!!
      val pipe = Angular2EntitiesProvider.getPipe(pipeClass)!!.sourceElement
      assertInstanceOf(HeadlessRenamePsiElementProcessor.processorOf(pipe), Angular2PipeRenameProcessor::class.java)

      val planned = HeadlessRenameProcessor.analyze(project, pipe, "bar")
      val plan = assertInstanceOf(planned, HeadlessRenameResult.Planned::class.java)

      assertInstanceOf(plan.plan.apply(), HeadlessRenameResult.Applied::class.java)
    }
}
