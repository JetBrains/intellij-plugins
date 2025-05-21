package org.jetbrains.qodana.ui.ci.providers.github

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.ui.ProjectVcsDataProvider
import org.jetbrains.qodana.ui.ci.providers.getSarifBaseline
import org.jetbrains.qodana.ui.ciRelevantBranches
import java.nio.file.Path

class DefaultQodanaGithubWorkflowBuilder(
  private val projectVcsDataProvider: ProjectVcsDataProvider,
  private val project: Project
) {

  suspend fun workflowFile(): String {
    val branchesToAdd = projectVcsDataProvider.ciRelevantBranches()

    @Language("YAML")
    val branchesText = """
      name: Qodana
      on:
        workflow_dispatch:
        pull_request:
        push:
          branches:
      
    """.trimIndent() + branchesToAdd.joinToString(separator = "\n", postfix = "\n") { "      - $it" }

    val jobText = qodanaJobText()

    @Suppress("UnnecessaryVariable")
    @Language("YAML")
    val yamlConfiguration = branchesText + """
      
      jobs:

    """.trimIndent() + jobText
    return yamlConfiguration
  }

  suspend fun qodanaJobText(): String {
    val cloudTokenText = "\${{ secrets.QODANA_TOKEN }}"
    val refsText = "\${{ github.event.pull_request.head.sha }}"

    val baselineText = getSarifBaseline(project)?.let { "args: --baseline,$it" }

    val qodanaGitHubActionVersion = ApplicationInfo.getInstance().shortVersion

    @Language("YAML")
    val jobText = """
      qodana:
        runs-on: ubuntu-latest
        permissions:
          contents: write
          pull-requests: write
          checks: write
        steps:
          - uses: actions/checkout@v3
            with:
              ref: ${refsText}
              fetch-depth: 0
          - name: 'Qodana Scan'
            uses: JetBrains/qodana-action@v$qodanaGitHubActionVersion
            env:
              QODANA_TOKEN: $cloudTokenText
            with:
              # In pr-mode: 'true' Qodana checks only changed files
              pr-mode: false
              use-caches: true
              post-pr-comment: true
              use-annotations: true
              # Upload Qodana results (SARIF, other artifacts, logs) as an artifact to the job
              upload-result: false
              # quick-fixes available in Ultimate and Ultimate Plus plans
              push-fixes: 'none'
            ${baselineText?.let { "  $it" } ?: ""}
    """.replaceIndent("  ").trimEnd()
    return jobText
  }

  fun getWorkflowFileLocation(): Path? =
    project.guessProjectDir()?.toNioPath()
      ?.resolve(GITHUB_WORKFLOWS_DIR)
      ?.resolve(DEFAULT_GITHUB_WORKFLOW_FILENAME)
}