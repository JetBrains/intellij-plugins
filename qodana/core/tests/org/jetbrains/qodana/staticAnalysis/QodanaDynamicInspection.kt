package org.jetbrains.qodana.staticAnalysis

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.*
import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalInspectionDescriptor
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolIssue
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsConfigurationProvider
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsProvider
import java.util.function.Consumer

fun registerDynamicExternalInspectionsInTests(configTester: ConfigTester, testRootDisposable: Disposable): DynamicExternalToolsProvider {
  val externalToolsProviderEP = ExternalToolsProvider.EXTERNAL_TOOLS_EP.point
  val externalToolsProviderInTests = DynamicExternalToolsProvider()
  externalToolsProviderEP.registerExtension(externalToolsProviderInTests, testRootDisposable)

  val externalToolsConfigurationEP = ExternalToolsConfigurationProvider.EP_NAME.point
  externalToolsConfigurationEP.registerExtension(DynamicExternalToolsConfigurationProvider(configTester), testRootDisposable)

  return externalToolsProviderInTests
}

class DynamicExternalToolsProvider: ExternalToolsProvider {
  override fun describeTools(project: Project?) = listOf(
    ExternalInspectionDescriptor(
      "inspection-1",
      "inspection-1",
      "Test Tooling",
      InspectionsBundle.message("group.names.probable.bugs"),
      "dummy description",
      true,
      HighlightDisplayLevel.WARNING
    ),
    ExternalInspectionDescriptor(
      "inspection-2",
      "inspection-2",
      "Test Tooling",
      InspectionsBundle.message("group.names.probable.bugs"),
      "dummy description",
      true,
      HighlightDisplayLevel.WARNING
    ),
    ExternalInspectionDescriptor(
      "inspection-3",
      "inspection-3",
      "Test Tooling",
      InspectionsBundle.message("group.names.probable.bugs"),
      "dummy description",
      true,
      HighlightDisplayLevel.WARNING
    )
  )

  override fun announceJobDescriptors(context: QodanaGlobalInspectionContext): Array<JobDescriptor> {
    return emptyArray()
  }

  override suspend fun runTools(context: QodanaGlobalInspectionContext, consumer: Consumer<ExternalToolIssue>) {
    val projectDir = context.project.guessProjectDir()?.canonicalPath ?: ""
    val fileNames = sequenceOf("B.java", "C.java", "D.java")
    for (i in 1 .. 3) {
      val inspectionId = """inspection-$i"""
      for (fileName in fileNames) {
        consumer.accept(ExternalToolIssue(inspectionId, HighlightDisplayLevel.WARNING, " ", "file://$projectDir/$fileName", language = "java"))
      }
    }
  }
}

class ConfigTester(var configured: Boolean = false, var deconfigured: Boolean = false)

class DynamicExternalToolsConfigurationProvider(private val testObject: ConfigTester): ExternalToolsConfigurationProvider {
  override fun announceJobDescriptors(context: QodanaGlobalInspectionContext): Array<JobDescriptor> {
    return emptyArray()
  }

  override suspend fun performPreRunActivities(context: QodanaGlobalInspectionContext) {
    testObject.configured = true
  }

  override suspend fun performPostRunActivities(context: QodanaGlobalInspectionContext) {
    testObject.deconfigured = true
  }
}