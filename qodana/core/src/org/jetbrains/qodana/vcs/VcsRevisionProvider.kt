package org.jetbrains.qodana.vcs

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

internal interface VcsRevisionProvider {
  companion object {
    val EP_NAME = ExtensionPointName<VcsRevisionProvider>("org.intellij.qodana.vcsRevisionProvider")
  }

  fun revisionUpdatedFlow(project: Project): Flow<Unit>

  suspend fun fetchHeadRevisions(project: Project, fromCommitIdx: Int, revisionsToFetchCount: Int): List<VcsRevision>
}

internal class VcsRevisionProviderAggregator(val providers: List<VcsRevisionProvider>) : VcsRevisionProvider {
  override fun revisionUpdatedFlow(project: Project): Flow<Unit> {
    return providers.map { it.revisionUpdatedFlow(project) }.merge()
  }

  override suspend fun fetchHeadRevisions(project: Project, fromCommitIdx: Int, revisionsToFetchCount: Int): List<VcsRevision> {
    return providers
      .flatMap { provider ->
        provider.fetchHeadRevisions(project, fromCommitIdx, revisionsToFetchCount)
      }
      .sortedByDescending { it.date }
  }
}