package org.jetbrains.qodana.staticAnalysis.inspections.targets

import com.intellij.codeInspection.InspectResultsConsumer
import com.intellij.codeInspection.ex.Tools
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jdom.Document
import org.jdom.input.SAXBuilder
import java.io.File

class QodanaTeamcityTestReporter : InspectResultsConsumer {
  override fun consume(tools: Map<String, Tools>, inspectionsResults: List<File>, project: Project) {
    val targetsService = project.service<QodanaTargetsService>()
    val targets = targetsService.getTestTargets(tools)
    //if (targets.isEmpty()) return
    //
    //val collector = ProblemsCollector(targets)
    //inspectionsResults.forEach {
    //  visitProblems(it) { problem ->
    //    collector.addProblem(problem)
    //  }
    //}
    //
    //collector.reportResults()
  }

  private fun visitProblems(
    file: File,
    visitor: (problem: Problem) -> Unit
  ) {
    val builder = SAXBuilder()
    val doc: Document = builder.build(file)
    val problems = doc.rootElement.getChildren("problem")
    problems.forEach { problem ->
      //val path = problem.getChildText("file")
      //val line = problem.getChildText("line")
      //val description = problem.getChildText("description")
      //val inspection = problem.getChild(INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT)?.getAttributeValue(INSPECTION_RESULTS_ID_ATTRIBUTE)
      //if (inspection != null && path != null) visitor.invoke(Problem(inspection, path, line, description))
    }
  }

  private class Problem(val inspection: String, val path: String, val line: String?, val description: String?) {
    val relativePath = path.replaceFirst("file://\$PROJECT_DIR\$/", "")
    fun getTestLineReport() = "- $description ; $relativePath:$line"
  }

  private class ProblemsCollector(val targets: List<TestTarget>) {
    val results = targets.map { it to mutableListOf<Problem>() }.toMap()

    fun addProblem(problem: Problem) {
      targets.forEach { addProblem(problem, it) }
    }

    fun addProblem(problem: Problem, target: TestTarget) {
      val problemsList = results[target] ?: return
      if (problemsList.size > target.threshold || !target.contains(problem.inspection, problem.path)) return
      problemsList.add(problem)
    }

    fun reportResults() {
      println("##teamcity[testSuiteStarted name='Inspections Tests']")
      results.forEach { (target, results) -> reportTarget(target, results) }
      println("##teamcity[testSuiteFinished name='Inspections Tests']")
    }

    fun reportTarget(target: TestTarget, problems: List<Problem>) {
      val groupedByInspection = problems.groupBy { it.inspection }
      target.inspections.forEach { inspection ->
        val testName = escape("${target.name}.$inspection")
        println("##teamcity[testStarted name='$testName']")
        val testCaseProblems = groupedByInspection[inspection] ?: emptyList()
        if (testCaseProblems.isNotEmpty()) {
          val problemsText = testCaseProblems.joinToString("\n") { it.getTestLineReport() }
          val message = escape("${testCaseProblems.size} problems found:\n$problemsText")
          println("##teamcity[testFailed name='$testName' message='$message' details='']")
          println("##teamcity[testMetadata testName='$testName' name='Problems' type='number' value='${testCaseProblems.size}']")
        }
        println("##teamcity[testFinished name='$testName']")
      }
    }

    private fun escape(line: String) = line
      .take(10_000)
      .replace("|", "||")
      .replace("[", "|[")
      .replace("]", "|]")
      .replace("\n", "|n")
      .replace("'", "|'")
      .replace("\r", "|r")
  }
}