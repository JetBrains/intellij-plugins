// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.codeInspection.ex.QuickFixWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.InspectionFixtureTestCase
import com.intellij.testFramework.InspectionTestUtil
import com.intellij.testFramework.assertEqualsToFile
import com.intellij.testFramework.createGlobalContextForTool
import com.intellij.testFramework.fixtures.impl.GlobalInspectionContextForTests
import junit.framework.TestCase
import java.io.File

abstract class TerraformInspectionFixtureTestCase : InspectionFixtureTestCase() {
  override fun doTest(testDir: String, tool: LocalInspectionTool) {
    val toolWrapper = LocalInspectionToolWrapper(tool)
    val sourceDir = myFixture.copyDirectoryToProject(File(testDir, "src").path, "")
    val psiDirectory = myFixture.psiManager.findDirectory(sourceDir)!!
    TestCase.assertNotNull(psiDirectory)
    val scope = AnalysisScope(psiDirectory)
    scope.invalidate()
    val globalContext = createGlobalContextForTool(scope, project, listOf<InspectionToolWrapper<*, *>>(toolWrapper))
    InspectionTestUtil.runTool(toolWrapper, scope, globalContext)
    InspectionTestUtil.compareToolResults(globalContext, toolWrapper, false, File(basePath, testDir).path)

    checkQuickFixes(globalContext, toolWrapper, sourceDir, testDir)
  }

  private fun checkQuickFixes(globalContext: GlobalInspectionContextForTests,
                              toolWrapper: LocalInspectionToolWrapper,
                              sourceDir: VirtualFile,
                              testDir: String) {
    for ((refEntity, descriptors) in globalContext.getPresentation(toolWrapper).problemElements.map) {
      for ((i, descriptor) in descriptors.withIndex()) {
        for (j in descriptor.fixes.orEmpty().indices) {
          val pd = descriptor as ProblemDescriptor
          myFixture.openFileInEditor(sourceDir.findChild(refEntity.name)!!)
          val intentionAction = QuickFixWrapper.wrap(pd, j)
          if (skipQuickFix(intentionAction)) continue
          if (skipCheckPreview(intentionAction))
            myFixture.launchAction(intentionAction)
          else
            myFixture.checkPreviewAndLaunchAction(intentionAction)

          assertEqualsToFile("quickfix ${intentionAction.text} result",
                             File(File(basePath, testDir), "after_${i}_${j}")
                               .apply { mkdirs() }
                               .resolve(refEntity.name),
                             myFixture.file.text)
        }
      }
    }
  }

  //TODO: Fix preview
  private val skipPreview = setOf("Add variable 'x'",
                                  "Add closing braces before element",
                                  "Rename output",
                                  "Convert to HCL2 expression",
                                  "Rename variable")

  open fun skipCheckPreview(intentionAction: IntentionAction): Boolean = intentionAction.text in skipPreview

  private val skipQuickFix = setOf("Save  to dictionary", "Navigate to  duplicate", "View duplicates like this")

  open fun skipQuickFix(intentionAction: IntentionAction): Boolean = intentionAction.text in skipQuickFix

}

