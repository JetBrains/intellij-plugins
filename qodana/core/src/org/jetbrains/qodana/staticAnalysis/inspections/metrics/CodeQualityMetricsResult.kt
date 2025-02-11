package org.jetbrains.qodana.staticAnalysis.inspections.metrics

import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.annotations.Nls
import org.jetbrains.qodana.QodanaBundle

private const val CODE_QUALITY_METRICS_FIELD_NAME = "metrics"

@Suppress("UNCHECKED_CAST")
var Run.codeQualityMetrics: Map<String, Any>
  set(value) {
    properties = (properties ?: PropertyBag()).also {
      it[CODE_QUALITY_METRICS_FIELD_NAME] = value
    }
  }
  get() = (properties?.get(CODE_QUALITY_METRICS_FIELD_NAME) as? Map<String, Any>?).orEmpty()

enum class CodeQualityMetrics(val prop: String, @Nls val title: String, @Nls val dim: String, val printable: Boolean = true) {
  LINES_OF_CODE(
    "linesOfCode",
    QodanaBundle.message("cli.metrics.lines.of.code.title"),
    QodanaBundle.message("cli.metrics.lines.of.code.lines")
  ),
  CYCLOMATIC_COMPLEXITY(
    "cyclomaticComplexity",
    QodanaBundle.message("cli.metrics.cyclomatic.complexity.title"),
    QodanaBundle.message("cli.metrics.cyclomatic.complexity.complexity")
  ),
  DFA_COMPLEXITY(
    "dfaComplexity",
    QodanaBundle.message("cli.metrics.dfa.complexity.title"),
    QodanaBundle.message("cli.metrics.dfa.complexity.complexity"),
    printable = false
  ),
  DFA_CONSISTENCY(
    "dfaConsistency",
    QodanaBundle.message("cli.metrics.dfa.consistency.title"),
    QodanaBundle.message("cli.metrics.dfa.consistency.consistency"),
    printable = false
  )
}
