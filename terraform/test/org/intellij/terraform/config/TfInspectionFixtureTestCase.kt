// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.CommonProblemDescriptor
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.QuickFix
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.codeInspection.ex.QuickFixWrapper
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.InspectionFixtureTestCase
import com.intellij.testFramework.InspectionTestUtil
import com.intellij.testFramework.assertEqualsToFile
import com.intellij.testFramework.createGlobalContextForTool
import com.intellij.testFramework.fixtures.impl.GlobalInspectionContextForTests
import junit.framework.TestCase
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import java.io.File

abstract class TfInspectionFixtureTestCase : InspectionFixtureTestCase() {
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
        for (j in descriptor.fixes.orEmpty<QuickFix<*>>().indices) {
          myFixture.openFileInEditor(sourceDir.findChild(refEntity.name)!!)
          val renewedDescriptor = rerunInspectionOnFile(toolWrapper)[i]
          val intentionAction = QuickFixWrapper.wrap(renewedDescriptor as ProblemDescriptor, j)

          if (skipQuickFix(intentionAction)) continue
          val fixInfo = """
            # intention: "${toolWrapper.shortName}"
            # fix: "${renewedDescriptor.fixes.orEmpty()[j].name}"
            # position: ${renewedDescriptor.lineNumber}: "${renewedDescriptor.psiElement.text.lines().firstOrNull()}"
            #
            
          """.trimIndent()

          if (skipCheckPreview(intentionAction))
            myFixture.launchAction(intentionAction)
          else
            myFixture.checkPreviewAndLaunchAction(intentionAction)

          assertEqualsToFile("quickfix ${intentionAction.text} result",
                             File(File(basePath, testDir), "after_${i}_${j}")
                               .apply { mkdirs() }
                               .resolve(refEntity.name),
                             fixInfo + myFixture.file.text)

          UndoManager.getInstance(project).undo(TextEditorProvider.getInstance().getTextEditor(editor))
          PsiDocumentManager.getInstance(project).commitAllDocuments()

        }
      }
    }
  }

  private fun rerunInspectionOnFile(toolWrapper: LocalInspectionToolWrapper): Array<out CommonProblemDescriptor> {
    val scope = AnalysisScope(myFixture.file)
    val context = createGlobalContextForTool(scope, project, listOf(toolWrapper))
    InspectionTestUtil.runTool(toolWrapper, scope, context)
    val presentation = context.getPresentation(toolWrapper)
    presentation.updateContent()
    return presentation.problemElements.map.values.single()
  }

  //TODO: Fix preview
  private val skipPreview = setOf(
    "Add variable 'x'",
    "Add closing braces before an element",
    "Rename output",
    "Convert to HCL2 expression",
    "Rename variable",
    "Run Terraform init",
  )

  open fun skipCheckPreview(intentionAction: IntentionAction): Boolean = intentionAction.text in skipPreview

  private val skipQuickFix = setOf(
    HCLBundle.message("duplicated.inspection.base.navigate.to.duplicate.quick.fix.name", ""),
    HCLBundle.message("duplicated.inspection.base.show.other.duplicates.quick.fix.name"),
    HCLBundle.message("action.TfInitRequiredAction.text", TfToolType.TERRAFORM.executableName),
    HCLBundle.message("disable.deep.variable.search"),
    HCLBundle.message("action.AddProviderAction.text"),
  )

  private val skipQuickFixRegexps = setOf(
    Regex("^Save '[a-zA-Z]*' to dictionary$")
  )

  open fun skipQuickFix(intentionAction: IntentionAction): Boolean = intentionAction.text in skipQuickFix
                                                                     || skipQuickFixRegexps.any { it.matches(intentionAction.text) }

}

