package org.jetbrains.qodana.inspectionKts

import com.intellij.analysis.AnalysisScope
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.*
import com.intellij.codeInspection.reference.RefEntity
import com.intellij.openapi.diagnostic.ControlFlowException
import com.intellij.openapi.diagnostic.logger
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.jetbrains.qodana.inspectionKts.api.InspectionKts
import org.jetbrains.qodana.inspectionKts.api.LocalInspectionImpl
import org.jetbrains.qodana.inspectionKts.api.LocalKtsInspectionTool

internal const val INSPECTIONS_KTS_GROUP_NAME = "FlexInspect"

internal fun InspectionKts.asTool(exceptionReporter: (Exception) -> Unit): InspectionProfileEntry {
  fun loggingExceptions(action: () -> Unit) {
    try {
      action.invoke()
    }
    catch (e : Exception) {
      if (e is ControlFlowException) {
        throw e
      }
      exceptionReporter.invoke(e)
      logger<InspectionKts>().error(e) // do not throw further, otherwise one incorrect inspection kills the whole analysis
    }
  }

  return when(val tool = this.tool) {
    is LocalInspectionTool -> {
      object : LocalInspectionTool() {
        override fun isEnabledByDefault(): Boolean = true

        override fun getLanguage(): String = this@asTool.language

        override fun getGroupDisplayName(): String = INSPECTIONS_KTS_GROUP_NAME

        override fun getDefaultLevel(): HighlightDisplayLevel = level

        override fun getShortName(): String = this@asTool.id

        override fun getDisplayName(): String = name

        override fun getStaticDescription(): String? = htmlDescription

        override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
          return tool.checkFile(file, manager, isOnTheFly)
        }

        override fun runForWholeFile(): Boolean = true

        override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
          if (tool !is LocalKtsInspectionTool) {
            return tool.buildVisitor(holder, isOnTheFly, session)
          }

          val inspection = LocalInspectionImpl(holder)
          return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
              loggingExceptions {
                tool.checker.invoke(file, inspection)
              }
            }
          }
        }
      }
    }
    is GlobalInspectionTool -> {
      object : GlobalInspectionTool() {
        override fun isGraphNeeded(): Boolean = tool.isGraphNeeded

        override fun isEnabledByDefault(): Boolean = true

        override fun getLanguage(): String = this@asTool.language

        override fun getGroupDisplayName(): String = INSPECTIONS_KTS_GROUP_NAME

        override fun getDefaultLevel(): HighlightDisplayLevel = level

        override fun getShortName(): String = id

        override fun getDisplayName(): String = name

        override fun getStaticDescription(): String? = htmlDescription

        override fun runInspection(
          scope: AnalysisScope,
          manager: InspectionManager,
          globalContext: GlobalInspectionContext,
          problemDescriptionsProcessor: ProblemDescriptionsProcessor
        ) {
          loggingExceptions {
            tool.runInspection(scope, manager, globalContext, problemDescriptionsProcessor)
          }
        }

        override fun checkElement(
          refEntity: RefEntity,
          scope: AnalysisScope,
          manager: InspectionManager,
          globalContext: GlobalInspectionContext
        ): Array<CommonProblemDescriptor>? {
          return tool.checkElement(refEntity, scope, manager, globalContext)
        }

        override fun checkElement(
          refEntity: RefEntity,
          scope: AnalysisScope,
          manager: InspectionManager,
          globalContext: GlobalInspectionContext,
          processor: ProblemDescriptionsProcessor
        ): Array<CommonProblemDescriptor>? {
          return tool.checkElement(refEntity, scope, manager, globalContext, processor)
        }
      }
    }
    else -> {
      error("""
        $INSPECTIONS_KTS_EXTENSION $this tool must be either 
        - Local inspection: ${LocalInspectionTool::class.java.canonicalName} 
        - Global inspection: ${GlobalInspectionTool::class.java.canonicalName}
              
        Got ${tool}
      """.trimIndent())
    }
  }
}