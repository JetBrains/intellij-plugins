package org.jetbrains.qodana.actions

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.jetbrains.qodana.sarif.model.Result.BaselineState
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.jetbrains.qodana.ui.QodanaTreeTestUtil
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeDirectoryNode
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeFileNode
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeInspectionCategoryNode
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeInspectionNode
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeNode
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeProblemNode
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeRoot
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeSeverityNode
import org.jetbrains.qodana.ui.problemsView.viewModel.impl.QodanaTreeBuildConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

private const val PROJECT_PATH = "QodanaSelectedContextBuilderTest/project"

@TestApplication
class QodanaSelectedContextBuilderTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  private val testDir by lazy { Path(qodanaFixture.fixture.testDataPath, "QodanaSelectedContextBuilderTest") }
  private val reportPath by lazy { testDir.resolve("report.sarif.json") }
  private val treeTestUtil by lazy { QodanaTreeTestUtil(qodanaFixture.project, testDir.resolve("project")) }

  @BeforeEach
  fun setUp() {
    qodanaFixture.fixture.copyDirectoryToProject(PROJECT_PATH, ".")
  }

  @Test
  fun `test root selected returns all problems`(): Unit = runDispatchingOnUi {
    val root = loadTree()
    val allProblems = root.collectAllProblems()

    val rootChain = listOf<QodanaTreeNode<*, *, *>>(root)
    val result = filterProblemsFromNodeChains(listOf(rootChain), allProblems)

    assertThat(result).containsExactlyInAnyOrderElementsOf(allProblems)
  }

  @Test
  fun `test single problem node selected`(): Unit = runDispatchingOnUi {
    val root = loadTree()
    val allProblems = root.collectAllProblems()

    val problemChain = findFirstChain(root) { it is QodanaTreeProblemNode }!!
    val expectedProblem = (problemChain.last() as QodanaTreeProblemNode).primaryData.sarifProblem

    val result = filterProblemsFromNodeChains(listOf(problemChain), allProblems)

    assertThat(result).containsExactly(expectedProblem)
  }

  @Test
  fun `test file node selected returns file problems`(): Unit = runDispatchingOnUi {
    // Use flat tree (no severity/inspection grouping) so the file selector is not diluted by ancestors
    val root = loadTree(groupBySeverity = false, groupByInspection = false)
    val allProblems = root.collectAllProblems()

    val fileChain = findFirstChain(root) { node ->
      node is QodanaTreeFileNode && node.primaryData.file.fileName.toString() == "Main.java"
    }!!
    val fileNode = fileChain.last() as QodanaTreeFileNode

    val result = filterProblemsFromNodeChains(listOf(fileChain), allProblems)

    assertThat(result).isNotEmpty
    assertThat(result).allSatisfy { problem ->
      assertThat(fileNode.isRelatedToProblem(problem)).isTrue()
    }
    assertThat(result).allSatisfy { problem ->
      assertThat(problem.relativePathToFile).isEqualTo("Main.java")
    }
  }

  @Test
  fun `test inspection node selected returns inspection problems`(): Unit = runDispatchingOnUi {
    // Use tree without severity grouping so inspection selector is not diluted by severity ancestor
    val root = loadTree(groupBySeverity = false)
    val allProblems = root.collectAllProblems()

    val inspectionChain = findFirstChain(root) { node ->
      node is QodanaTreeInspectionNode && node.primaryData.inspectionId == "INSPECTION_ID"
    }!!
    val inspectionNode = inspectionChain.last() as QodanaTreeInspectionNode

    val result = filterProblemsFromNodeChains(listOf(inspectionChain), allProblems)

    assertThat(result).isNotEmpty
    assertThat(result).allSatisfy { problem ->
      assertThat(inspectionNode.isRelatedToProblem(problem)).isTrue()
    }
    val inspection2Problems = allProblems.filter { it.inspectionId == "INSPECTION_ID2" }
    assertThat(result).doesNotContainAnyElementsOf(inspection2Problems)
  }

  @Test
  fun `test severity node selected returns all problems of that severity`(): Unit = runDispatchingOnUi {
    val root = loadTree()
    val allProblems = root.collectAllProblems()

    val severityChain = findFirstChain(root) { it is QodanaTreeSeverityNode }!!
    val severityNode = severityChain.last() as QodanaTreeSeverityNode

    val result = filterProblemsFromNodeChains(listOf(severityChain), allProblems)

    assertThat(result).isNotEmpty
    assertThat(result).allSatisfy { problem ->
      assertThat(severityNode.isRelatedToProblem(problem)).isTrue()
    }
  }

  @Test
  fun `test directory node selected returns directory problems`(): Unit = runDispatchingOnUi {
    // Use flat tree (no severity/inspection grouping) so the directory selector is not diluted by ancestors
    val root = loadTree(groupBySeverity = false, groupByInspection = false)
    val allProblems = root.collectAllProblems()

    val dirChain = findFirstChain(root) { node ->
      node is QodanaTreeDirectoryNode && node.primaryData.ownPath.toString() == "anotherModule"
    }!!
    val dirNode = dirChain.last() as QodanaTreeDirectoryNode

    val result = filterProblemsFromNodeChains(listOf(dirChain), allProblems)

    assertThat(result).isNotEmpty
    assertThat(result).allSatisfy { problem ->
      assertThat(dirNode.isRelatedToProblem(problem)).isTrue()
    }
  }

  @Test
  fun `test nested selection dedup keeps only parent`(): Unit = runDispatchingOnUi {
    val root = loadTree()
    val allProblems = root.collectAllProblems()

    // Select an inspection node and a file node under it
    val inspectionChain = findFirstChain(root) { node ->
      node is QodanaTreeInspectionNode && node.primaryData.inspectionId == "INSPECTION_ID"
    }!!
    val fileChain = findFirstChain(root) { node ->
      node is QodanaTreeFileNode && node.primaryData.file.fileName.toString() == "Main.java"
    }!!

    // Only take the file chain that is actually under the inspection chain
    val fileUnderInspection = findAllChains(root) { node ->
      node is QodanaTreeFileNode && node.primaryData.file.fileName.toString() == "Main.java"
    }.firstOrNull { chain ->
      chain.any { it is QodanaTreeInspectionNode && it.primaryData.inspectionId == "INSPECTION_ID" }
    } ?: fileChain

    // If file is under inspection, selecting both should give same result as selecting inspection alone
    if (isProperPrefix(inspectionChain, fileUnderInspection)) {
      val resultBoth = filterProblemsFromNodeChains(listOf(inspectionChain, fileUnderInspection), allProblems)
      val resultInspectionOnly = filterProblemsFromNodeChains(listOf(inspectionChain), allProblems)

      assertThat(resultBoth).containsExactlyInAnyOrderElementsOf(resultInspectionOnly)
    }
  }

  @Test
  fun `test multiple disjoint category nodes selected returns union`(): Unit = runDispatchingOnUi {
    // Use tree without severity grouping so individual inspection selectors are distinct
    val root = loadTree(groupBySeverity = false)
    val allProblems = root.collectAllProblems()

    val allInspectionChains = findAllChains(root) { it is QodanaTreeInspectionNode }
    assertThat(allInspectionChains.size).isGreaterThanOrEqualTo(2)

    val chain1 = allInspectionChains[0]
    val chain2 = allInspectionChains[1]

    val resultCombined = filterProblemsFromNodeChains(listOf(chain1, chain2), allProblems)
    val result1 = filterProblemsFromNodeChains(listOf(chain1), allProblems)
    val result2 = filterProblemsFromNodeChains(listOf(chain2), allProblems)

    assertThat(resultCombined).containsExactlyInAnyOrderElementsOf((result1 + result2).distinct())
  }

  @Test
  fun `test mixed category and problem nodes`(): Unit = runDispatchingOnUi {
    val root = loadTree()
    val allProblems = root.collectAllProblems()

    // Select inspection INSPECTION_ID2 as a category
    val inspectionChain = findFirstChain(root) { node ->
      node is QodanaTreeInspectionNode && node.primaryData.inspectionId == "INSPECTION_ID2"
    }!!

    // Select a specific problem from INSPECTION_ID (disjoint from the category)
    val problemChain = findAllChains(root) { node ->
      node is QodanaTreeProblemNode && node.primaryData.sarifProblem.inspectionId == "INSPECTION_ID"
    }.first()
    val directProblem = (problemChain.last() as QodanaTreeProblemNode).primaryData.sarifProblem

    val result = filterProblemsFromNodeChains(listOf(inspectionChain, problemChain), allProblems)

    // Should contain the directly selected problem
    assertThat(result).contains(directProblem)
    // Should contain problems from INSPECTION_ID2
    val inspection2Problems = allProblems.filter { it.inspectionId == "INSPECTION_ID2" }
    assertThat(result).containsAll(inspection2Problems)
  }

  @Test
  fun `test inspection category node selected returns category problems`(): Unit = runDispatchingOnUi {
    // Use tree without severity grouping so category selector is not diluted by severity ancestor
    val root = loadTree(groupBySeverity = false)
    val allProblems = root.collectAllProblems()

    val categoryChain = findFirstChain(root) { node ->
      node is QodanaTreeInspectionCategoryNode && node.primaryData.categoryId == "CATEGORY_A"
    }!!
    val categoryNode = categoryChain.last() as QodanaTreeInspectionCategoryNode

    val result = filterProblemsFromNodeChains(listOf(categoryChain), allProblems)

    assertThat(result).isNotEmpty
    assertThat(result).allSatisfy { problem ->
      assertThat(categoryNode.isRelatedToProblem(problem)).isTrue()
    }
    // CATEGORY_A contains only INSPECTION_ID, so INSPECTION_ID2 problems should not be included
    val categoryBProblems = allProblems.filter { it.inspectionId == "INSPECTION_ID2" }
    assertThat(result).doesNotContainAnyElementsOf(categoryBProblems)
  }

  @Test
  fun `test empty chains returns empty`() {
    val allProblems = setOf(createTestSarifProblem("test.java", "TestInspection"))

    val result = filterProblemsFromNodeChains(emptyList(), allProblems)

    assertThat(result).isEmpty()
  }

  @Test
  fun `test selector from file chain matches only file problems`(): Unit = runDispatchingOnUi {
    // Use flat tree so the file chain has no severity/inspection ancestors
    val root = loadTree(groupBySeverity = false, groupByInspection = false)
    val allProblems = root.collectAllProblems()

    val fileChain = findFirstChain(root) { node ->
      node is QodanaTreeFileNode && node.primaryData.file.fileName.toString() == "Main.java"
    }!!

    val selector = buildSelectorFromNodeChain(fileChain)

    val matched = allProblems.filter(selector)
    assertThat(matched).isNotEmpty
    assertThat(matched).allSatisfy { assertThat(it.relativePathToFile).isEqualTo("Main.java") }
  }

  @Test
  fun `test selector from inspection chain matches only inspection problems`(): Unit = runDispatchingOnUi {
    // Use tree without severity grouping so the inspection chain only includes inspection ancestor
    val root = loadTree(groupBySeverity = false)
    val allProblems = root.collectAllProblems()

    val inspectionChain = findFirstChain(root) { node ->
      node is QodanaTreeInspectionNode && node.primaryData.inspectionId == "INSPECTION_ID2"
    }!!

    val selector = buildSelectorFromNodeChain(inspectionChain)

    val matched = allProblems.filter(selector)
    assertThat(matched).isNotEmpty
    assertThat(matched).allSatisfy { assertThat(it.inspectionId).isEqualTo("INSPECTION_ID2") }
  }

  @Test
  fun `test selector from category chain matches only category problems`(): Unit = runDispatchingOnUi {
    // Use tree without severity grouping so category chain only has category ancestor
    val root = loadTree(groupBySeverity = false)
    val allProblems = root.collectAllProblems()

    val categoryChain = findFirstChain(root) { node ->
      node is QodanaTreeInspectionCategoryNode && node.primaryData.categoryId == "CATEGORY_B"
    }!!

    val selector = buildSelectorFromNodeChain(categoryChain)

    val matched = allProblems.filter(selector)
    assertThat(matched).isNotEmpty
    // CATEGORY_B contains only INSPECTION_ID2
    assertThat(matched).allSatisfy { assertThat(it.inspectionId).isEqualTo("INSPECTION_ID2") }
  }

  @Test
  fun `test toContextProblem maps all fields correctly`() {
    val problem = createTestSarifProblem("src/Main.java", "NullCheck")

    val contextProblem = problem.toContextProblem()

    assertThat(contextProblem.startLine).isEqualTo(problem.startLine)
    assertThat(contextProblem.startColumn).isEqualTo(problem.startColumn)
    assertThat(contextProblem.path).isEqualTo(problem.relativePathToFile)
    assertThat(contextProblem.message).isEqualTo(problem.message)
    assertThat(contextProblem.qodanaSeverity).isEqualTo(problem.qodanaSeverity)
    assertThat(contextProblem.inspectionId).isEqualTo(problem.inspectionId)
    assertThat(contextProblem.baselineState).isEqualTo(problem.baselineState)
  }

  @Test
  fun `test toContextProblem empty traces become null`() {
    val problem = createTestSarifProblem("test.java", "TestInspection", traces = emptyList())

    val contextProblem = problem.toContextProblem()

    assertThat(contextProblem.traces).isNull()
  }

  private suspend fun loadTree(
    groupBySeverity: Boolean = true,
    groupByInspection: Boolean = true,
    groupByModule: Boolean = true,
    groupByDirectory: Boolean = true,
  ): QodanaTreeRoot {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      showBaselineProblems = true,
      groupBySeverity = groupBySeverity,
      groupByInspection = groupByInspection,
      groupByModule = groupByModule,
      groupByDirectory = groupByDirectory
    )
    return treeTestUtil.loadProblems(reportPath, treeConfiguration)!!
  }

  private fun QodanaTreeRoot.collectAllProblems(): Set<SarifProblem> {
    return flattenTree().filterIsInstance<QodanaTreeProblemNode>()
      .map { it.primaryData.sarifProblem }
      .toSet()
  }

  private fun QodanaTreeRoot.flattenTree(): List<QodanaTreeNode<*, *, *>> {
    val result = mutableListOf<QodanaTreeNode<*, *, *>>()
    val queue = ArrayDeque<QodanaTreeNode<*, *, *>>()
    queue.addFirst(this)
    while (queue.isNotEmpty()) {
      val node = queue.removeFirst()
      result.add(node)
      node.children.nodesSequence.forEach { queue.addLast(it) }
    }
    return result
  }

  private fun findFirstChain(
    root: QodanaTreeRoot,
    predicate: (QodanaTreeNode<*, *, *>) -> Boolean,
  ): List<QodanaTreeNode<*, *, *>>? {
    fun dfs(node: QodanaTreeNode<*, *, *>, path: List<QodanaTreeNode<*, *, *>>): List<QodanaTreeNode<*, *, *>>? {
      val currentPath = path + node
      if (predicate(node)) return currentPath
      for (child in node.children.nodesSequence) {
        val result = dfs(child, currentPath)
        if (result != null) return result
      }
      return null
    }
    return dfs(root, emptyList())
  }

  private fun findAllChains(
    root: QodanaTreeRoot,
    predicate: (QodanaTreeNode<*, *, *>) -> Boolean,
  ): List<List<QodanaTreeNode<*, *, *>>> {
    val results = mutableListOf<List<QodanaTreeNode<*, *, *>>>()
    fun dfs(node: QodanaTreeNode<*, *, *>, path: List<QodanaTreeNode<*, *, *>>) {
      val currentPath = path + node
      if (predicate(node)) results.add(currentPath)
      for (child in node.children.nodesSequence) {
        dfs(child, currentPath)
      }
    }
    dfs(root, emptyList())
    return results
  }

  private fun isProperPrefix(
    shorter: List<QodanaTreeNode<*, *, *>>,
    longer: List<QodanaTreeNode<*, *, *>>,
  ): Boolean {
    if (shorter.size >= longer.size) return false
    return shorter.indices.all { i -> shorter[i].primaryData == longer[i].primaryData }
  }

  private fun createTestSarifProblem(
    path: String,
    inspectionId: String,
    traces: Collection<org.jetbrains.qodana.problem.SarifTrace> = emptyList(),
  ): SarifProblem {
    return SarifProblem(
      startLine = 10,
      startColumn = 5,
      endLine = 10,
      endColumn = 15,
      charLength = 10,
      relativePathToFile = path,
      traces = traces,
      message = "Test problem message",
      qodanaSeverity = QodanaSeverity.HIGH,
      inspectionId = inspectionId,
      baselineState = BaselineState.NEW,
      snippetText = null,
      revisionId = null
    )
  }
}
