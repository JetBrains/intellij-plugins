package org.jetbrains.qodana.vcs

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.blockingContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.VcsException
import com.intellij.vcs.log.TimedVcsCommit
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryChangeListener
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import java.time.Instant

internal class GitRevisionProvider : VcsRevisionProvider {
  override fun revisionUpdatedFlow(project: Project): Flow<Unit> {
    return callbackFlow {
      val disposable = Disposer.newDisposable()
      project.messageBus.connect(disposable).subscribe(GitRepository.GIT_REPO_CHANGE,
          GitRepositoryChangeListener {
            trySendBlocking(Unit)
          }
      )
      awaitClose { Disposer.dispose(disposable) }
    }.buffer(Channel.CONFLATED)
  }

  override suspend fun fetchHeadRevisions(project: Project, fromCommitIdx: Int, revisionsToFetchCount: Int): List<VcsRevision> {
    return GitRepositoryManager.getInstance(project).repositories
      .flatMap { repository ->
        val commits: List<TimedVcsCommit?> = withContext(QodanaDispatchers.IO) {
          blockingContext {
            try {
              GitHistoryUtils.collectTimedCommits(project, repository.root, "--max-count=$revisionsToFetchCount", "HEAD~$fromCommitIdx")
            }
            catch (e : VcsException) {
              thisLogger().warn("Failed loading revisions", e)
              emptyList()
            }
          }
        }
        commits
          .filterNotNull()
          .map {
            VcsRevision(
              it.id.asString(),
              Instant.ofEpochMilli(it.timestamp)
            )
          }
      }
  }
}