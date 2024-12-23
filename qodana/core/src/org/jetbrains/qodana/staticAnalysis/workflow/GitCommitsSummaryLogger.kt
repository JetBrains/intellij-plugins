package org.jetbrains.qodana.staticAnalysis.workflow

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vcs.VcsException
import git4idea.repo.GitRepositoryManager
import git4idea.statistics.GitCommitterCounter
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.stat.QodanaProjectInfoCollector.logAbsentHistorySummary
import org.jetbrains.qodana.staticAnalysis.stat.QodanaProjectInfoCollector.logCommitsSummary
import java.time.Period

/**
 * Sends to FUS data about number of commits and unique authors for last 30, 60 and 90 days.
 * Numbers are obtained with 'git shortlog -s --since "%since%"' command.
 * Since syntax https://github.com/git/git/blob/master/date.c#L131
 */
private class GitCommitsSummaryLogger : QodanaWorkflowExtension {
  override suspend fun beforeLaunch(context: QodanaRunContext) {
    val project = context.project
    val repos = GitRepositoryManager.getInstance(project).repositories
    if (repos.size == 0) {
      logAbsentHistorySummary(project)
      return
    }

    val repo = repos[0]

    try {
      runInterruptible(StaticAnalysisDispatchers.IO) {
        val commitCount = GitCommitterCounter(listOf(Period.ofDays(30), Period.ofDays(60), Period.ofDays(90)),
                                              collectCommitCount = true,
                                              additionalGitParameters = listOf("HEAD")).calculateWithGit(project, repo)

        logCommitsSummary(
          project,
          commitCount[0].authors,
          commitCount[1].authors,
          commitCount[2].authors,
          commitCount[0].commits,
          commitCount[1].commits,
          commitCount[2].commits
        )
      }
    }
    catch (e: VcsException) {
      logAbsentHistorySummary(project)
      thisLogger().warn(e)
    }
  }
}