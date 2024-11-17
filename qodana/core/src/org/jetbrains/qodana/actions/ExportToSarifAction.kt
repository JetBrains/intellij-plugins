// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.actions

import com.intellij.codeInspection.InspectionsBundle
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ui.InspectionNode
import com.intellij.codeInspection.ui.InspectionTree
import com.intellij.codeInspection.ui.actions.InspectionResultsExportActionProvider
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.util.text.StringUtil
import com.intellij.project.stateStore
import com.jetbrains.qodana.sarif.SarifUtil.writeReport
import com.jetbrains.qodana.sarif.model.*
import icons.QodanaIcons
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Supplier

@Suppress("ComponentNotRegistered")
class ExportToSarifAction : InspectionResultsExportActionProvider(Supplier { "Sarif" },
                                                                  InspectionsBundle.messagePointer("inspection.action.export.sarif.description"),
                                                                  QodanaIcons.Icons.Sarif) {
  override val progressTitle: String = InspectionsBundle.message("inspection.generating.sarif.progress.title")

  override fun writeResults(tree: InspectionTree,
                            profile: InspectionProfileImpl,
                            globalInspectionContext: GlobalInspectionContextImpl,
                            project: Project,
                            outputPath: Path) {
    val file = File(outputPath.toFile(), "report_${SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(Date())}.sarif.json")
    writeReport(file.toPath(), createSarifReport(tree, profile, globalInspectionContext))
  }

  companion object {
    fun createSarifReport(tree: InspectionTree, profile: InspectionProfileImpl, globalInspectionContext: GlobalInspectionContextImpl): SarifReport {
      val appInfo = ApplicationInfo.getInstance()
      val basePath = globalInspectionContext.project.stateStore.projectBasePath.toUri().toString()

      // Tool (current IDE)
      val run = Run()
        .withTool(
          Tool().withDriver(
            ToolComponent()
              .withName(appInfo.versionName)
              .withInformationUri(URI(appInfo.companyURL))
              .withVersion(appInfo.build.asStringWithoutProductCode())
              .withRules(mutableListOf())
          ))
        .withInvocations(
          listOf(
            Invocation()
              .withExecutionSuccessful(true)
              .withWorkingDirectory(
                ArtifactLocation()
                  .withUri(basePath)
              )
          )
        )
        .withResults(mutableListOf())

      val addedRules = hashSetOf<String>()

      val addResults = { tool: InspectionToolWrapper<*, *> ->
        globalInspectionContext
          .getPresentation(tool)
          .problemDescriptors
          .map { descriptor ->
            if (!addedRules.contains(tool.id)) {
              // Rule (inspection)
              run.tool.driver.rules.add(
                ReportingDescriptor()
                  .withName(tool.displayName)
                  .withId(tool.id)
              )
              addedRules.add(tool.id)
            }

            // Result (inspection result)
            val result = Result()
              .withMessage(Message().withText(descriptor.toString()))
              .withLevel(tool.defaultLevel.severity.toLevel())
              .withRuleId(tool.id)

            if (descriptor is ProblemDescriptorBase) {
              descriptor.psiElement?.let {
                val lineColumn = StringUtil.offsetToLineColumn(it.containingFile.text, it.textOffset)
                val lineColumnEnd = StringUtil.offsetToLineColumn(it.containingFile.text, it.textOffset + it.textLength)
                val relativePath = FileUtilRt.getRelativePath(basePath, it.containingFile.virtualFile.url, File.separatorChar)

                // Location (file & PSI element location)
                result.locations = listOf(
                  Location().withPhysicalLocation(
                    PhysicalLocation()
                      .withArtifactLocation(
                        ArtifactLocation().withUri(
                          if (relativePath.isNullOrBlank()) it.containingFile.virtualFile.url
                          else relativePath
                        )
                      )
                      .withRegion(
                        Region()
                          .withCharOffset(it.textOffset)
                          .withCharLength(it.textLength)
                          .withStartLine(lineColumn.line + 1)
                          .withStartColumn(lineColumn.column + 1)
                          .withEndLine(lineColumnEnd.line + 1)
                          .withEndColumn(lineColumnEnd.column + 1)
                          .withSnippet(ArtifactContent().withText(it.text))
                      )
                  )
                )
              }
            }

            run.results.add(result)
          }
      }

      if (profile.singleTool != null) {
        globalInspectionContext.tools[profile.singleTool]?.let {
          addResults(it.tool)
        }
      } else {
        tree.inspectionTreeModel
          .traverse(tree.inspectionTreeModel.root)
          .filter(InspectionNode::class.java)
          .forEach { node ->
            addResults(node.toolWrapper)
          }
      }

      val schema = URI("https://raw.githubusercontent.com/schemastore/schemastore/master/src/schemas/json/sarif-2.1.0-rtm.5.json")
      return SarifReport(SarifReport.Version._2_1_0, listOf(run)).`with$schema`(schema)
    }

    private fun HighlightSeverity.toLevel(): Level {
      return when (this) {
        HighlightSeverity.ERROR -> Level.ERROR
        HighlightSeverity.WARNING -> Level.WARNING
        HighlightSeverity.WEAK_WARNING -> Level.NOTE
        else -> Level.NOTE
      }
    }
  }
}