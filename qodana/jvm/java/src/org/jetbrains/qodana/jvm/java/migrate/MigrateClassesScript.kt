package org.jetbrains.qodana.jvm.java.migrate

import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Ref
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.refactoring.RefactoringManager
import com.intellij.refactoring.migration.MigrationProcessor
import com.intellij.usageView.UsageInfo
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.script.*

internal class MigrateClassesScriptFactory : QodanaScriptFactory {
  companion object {
    private const val MIGRATION_MAP_ARG = "include-mapping"
    const val SCRIPT_NAME = "migrate-classes"
  }

  override val scriptName: String get() = SCRIPT_NAME

  override fun parseParameters(parameters: String): Map<String, String> {
    if (parameters.isBlank()) throw QodanaException(
      "CLI parameter for ${SCRIPT_NAME} must be passed as '--script ${SCRIPT_NAME}:%migrationName%'. " +
      "For example '--script ${SCRIPT_NAME}:Java EE to Jakarta EE'."
    )
    return mapOf(MIGRATION_MAP_ARG to parameters)
  }

  override fun createScript(
    config: QodanaConfig,
    messageReporter: QodanaMessageReporter,
    contextFactory: QodanaRunContextFactory,
    parameters: UnvalidatedParameters
  ): QodanaScript = MigrateClassesScript(messageReporter, MigrationParameters.fromParameters(parameters), contextFactory)
}

internal class MigrateClassesScript(
  private val messageReporter: QodanaMessageReporter,
  private val parameters: MigrationParameters,
  runContextFactory: QodanaRunContextFactory
) : QodanaSingleRunScript(runContextFactory, AnalysisKind.OTHER) {

  override suspend fun execute(
    report: SarifReport,
    run: Run,
    runContext: QodanaRunContext,
    inspectionContext: QodanaGlobalInspectionContext
  ) {
    val map = runContext.project.serviceAsync<RefactoringManager>()
      .migrateManager
      .let(parameters::resolveMap)

    val processor: BaseRefactoringProcessor = object : MigrationProcessor(runContext.project, map) {
      override fun preprocessUsages(usages: Ref<Array<UsageInfo>>): Boolean {
        val usages = usages.get()
        val foundAny = usages.isNotEmpty()

        if (foundAny) {
          val files = usages.asSequence()
            .distinctBy(UsageInfo::getVirtualFile)
            .count()

          messageReporter.reportMessage(
            1,
            QodanaBundle.message("script.migrate.classes.found.usages", usages.size, files)
          )
        }
        else {
          messageReporter.reportMessage(
            1,
            QodanaBundle.message("script.migrate.classes.no.usages")
          )
        }

        return foundAny
      }
    }

    runTaskAndLogTime("'${map.name}'") {
      withContext(StaticAnalysisDispatchers.UI) {
        writeIntentReadAction {
          processor.run()
          FileDocumentManager.getInstance().saveAllDocuments()
        }
      }
    }
  }
}
