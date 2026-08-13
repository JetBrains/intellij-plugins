package org.jetbrains.qodana.dart

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalInspectionDescriptor
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolIssue
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsProvider
import java.util.function.Consumer

internal const val DART_ANALYZE_SHORT_NAME = "DartAnalyze"
internal const val DART_PROJECT_SETUP_SHORT_NAME = "DartProjectSetup"
internal const val DART_ANALYZE_UNCATEGORIZED_SHORT_NAME = "DartAnalyzeUncategorized"

class DartAnalyzeInspectionDescriptorProvider : ExternalToolsProvider {
  override fun describeTools(project: Project?): List<ExternalInspectionDescriptor> = DartAnalyzeInspectionCatalog.descriptors()

  override fun announceJobDescriptors(context: QodanaGlobalInspectionContext): Array<JobDescriptor> = emptyArray()

  override suspend fun runTools(context: QodanaGlobalInspectionContext, consumer: Consumer<ExternalToolIssue>) {
  }
}

internal object DartAnalyzeInspectionCatalog {
  private const val LINT_CATALOG_RESOURCE = "/dart-analyze/lint-rules.txt"
  private const val CORE_LINTS = "core"
  private const val RECOMMENDED_LINTS = "recommended"
  private const val FLUTTER_LINTS = "flutter"

  private val lintRules: Map<String, List<LintRule>> = loadLintRules()
  private val lintDescriptions: Map<String, String> = lintRules.values.flatten().associate { it.code to it.description }
  val coreLintCodes: Set<String> = lintRules.getValue(CORE_LINTS).mapToSortedSet()
  val recommendedLintCodes: Set<String> = lintRules.getValue(RECOMMENDED_LINTS).mapToSortedSet()
  val flutterLintCodes: Set<String> = lintRules.getValue(FLUTTER_LINTS).mapToSortedSet()
  val categorizedCodes: Set<String> = (coreLintCodes + recommendedLintCodes + flutterLintCodes).toSortedSet()

  val inspectionIds: List<String> = categorizedCodes.map(::inspectionId)

  fun inspectionIdFor(code: String): String = if (code in categorizedCodes) inspectionId(code) else DART_ANALYZE_UNCATEGORIZED_SHORT_NAME

  fun descriptors(): List<ExternalInspectionDescriptor> = buildList {
    add(
      ExternalInspectionDescriptor(
        DART_PROJECT_SETUP_SHORT_NAME,
        QodanaDartBundle.message("inspection.dart.project.setup.name"),
        QodanaDartBundle.message("inspection.dart.analyze.group.name"),
        QodanaDartBundle.message("inspection.dart.analyze.group.name"),
        QodanaDartBundle.message("inspection.dart.project.setup.description"),
        true,
        HighlightDisplayLevel.ERROR,
      )
    )
    add(
      ExternalInspectionDescriptor(
        DART_ANALYZE_UNCATEGORIZED_SHORT_NAME,
        QodanaDartBundle.message("inspection.dart.analyze.uncategorized.name"),
        QodanaDartBundle.message("inspection.dart.analyze.group.name"),
        QodanaDartBundle.message("inspection.dart.analyze.group.name"),
        QodanaDartBundle.message("inspection.dart.analyze.uncategorized.description"),
        false,
        HighlightDisplayLevel.WARNING,
      )
    )
    coreLintCodes.forEach { code -> addDiagnosticDescriptor(code, lintGroupPath(), coreLintGroupName()) }
    recommendedLintCodes.forEach { code -> addDiagnosticDescriptor(code, lintGroupPath(), recommendedLintGroupName()) }
    flutterLintCodes.forEach { code -> addDiagnosticDescriptor(code, lintGroupPath(), flutterLintGroupName()) }
  }

  private fun MutableList<ExternalInspectionDescriptor>.addDiagnosticDescriptor(
    code: String,
    @Nls groupPath: String,
    @Nls groupDisplayName: String,
  ) {
    add(
      ExternalInspectionDescriptor(
        inspectionId(code),
        QodanaDartBundle.message("inspection.dart.analyze.diagnostic.name", code),
        groupPath,
        groupDisplayName,
        QodanaDartBundle.message("inspection.dart.analyze.diagnostic.description", code, lintDescriptions.getValue(code)),
        true,
        HighlightDisplayLevel.WARNING,
      )
    )
  }

  @Nls
  private fun lintGroupPath(): String = "${QodanaDartBundle.message("inspection.dart.analyze.group.name")},${QodanaDartBundle.message("inspection.dart.analyze.lints.group.name")}"

  @Nls
  private fun coreLintGroupName(): String = QodanaDartBundle.message("inspection.dart.analyze.lints.core.group.name")

  @Nls
  private fun recommendedLintGroupName(): String = QodanaDartBundle.message("inspection.dart.analyze.lints.recommended.group.name")

  @Nls
  private fun flutterLintGroupName(): String = QodanaDartBundle.message("inspection.dart.analyze.lints.flutter.group.name")

  private fun loadLintRules(): Map<String, List<LintRule>> {
    val rules = requireNotNull(javaClass.getResourceAsStream(LINT_CATALOG_RESOURCE)) {
      "Missing Dart lint catalog: $LINT_CATALOG_RESOURCE"
    }.bufferedReader().useLines { lines ->
      lines.map(String::trim).filter { it.isNotBlank() && !it.startsWith('#') }.map { line ->
      val separator = line.indexOf(": ")
      require(separator > 0) { "Invalid Dart lint rule declaration: $line" }
      val descriptionSeparator = line.indexOf(" | ", separator + 2)
      require(descriptionSeparator > separator) { "Missing Dart lint rule description: $line" }
      LintRule(
        line.substring(0, separator),
        line.substring(separator + 2, descriptionSeparator),
        line.substring(descriptionSeparator + 3),
      )
      }.groupBy(LintRule::category).mapValues { (_, rules) -> rules.sortedBy(LintRule::code) }
    }
    require(rules.keys == setOf(CORE_LINTS, RECOMMENDED_LINTS, FLUTTER_LINTS)) { "Unexpected Dart lint rule categories: ${rules.keys}" }
    require(coreLintCodesDoNotOverlapRecommended(rules)) { "Dart core and recommended lint rules must not overlap" }
    require(lintCodesDoNotOverlap(rules)) { "Dart lint rule categories must not overlap" }
    return rules
  }

  private fun coreLintCodesDoNotOverlapRecommended(rules: Map<String, List<LintRule>>): Boolean =
    rules.getValue(CORE_LINTS).map(LintRule::code).toSet().intersect(rules.getValue(RECOMMENDED_LINTS).map(LintRule::code).toSet()).isEmpty()

  private fun lintCodesDoNotOverlap(rules: Map<String, List<LintRule>>): Boolean =
    rules.values.flatten().map(LintRule::code).let { it.size == it.toSet().size }

  private fun List<LintRule>.mapToSortedSet(): Set<String> = mapTo(sortedSetOf(), LintRule::code)

  private data class LintRule(val category: String, val code: String, val description: String)

  private fun inspectionId(code: String): String = "$DART_ANALYZE_SHORT_NAME.$code"
}
