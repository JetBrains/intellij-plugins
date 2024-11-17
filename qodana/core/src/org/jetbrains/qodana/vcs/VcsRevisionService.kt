@file:OptIn(ExperimentalCoroutinesApi::class)

package org.jetbrains.qodana.vcs

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.epUpdatedFlow
import org.jetbrains.qodana.registry.QodanaRegistry

private val LOG = logger<VcsRevisionService>()

@Service(Service.Level.PROJECT)
internal class VcsRevisionService(private val project: Project, scope: CoroutineScope) {
  companion object {
    fun getInstance(project: Project): VcsRevisionService = project.service()
  }

  val revisionPagedLoaderFlow: SharedFlow<VcsRevisionPagedLoader> = revisionPagedLoaderFlow()
    .flowOn(QodanaDispatchers.Default)
    .shareIn(scope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

  private fun revisionPagedLoaderFlow(): Flow<VcsRevisionPagedLoader> {
    val ep = VcsRevisionProvider.EP_NAME
    return ep.epUpdatedFlow()
      .onStart { emit(Unit) }
      .flatMapLatest {
        val revisionProviderAggregator = VcsRevisionProviderAggregator(ep.extensionList)

        revisionProviderAggregator.revisionUpdatedFlow(project)
          .onStart { emit(Unit) }
          .transformLatest {
            coroutineScope {
              val loader = VcsRevisionPagedLoader(
                scope = this@coroutineScope,
                project,
                revisionProviderAggregator,
                pageSize = QodanaRegistry.vcsRevisionPageSize,
                maxPages = QodanaRegistry.vcsRevisionMaxPages
              )
              emit(loader)
              awaitCancellation()
            }
          }
      }
      .onEach {
        LOG.debug { "Resetting Qodana VCS paged loader" }
      }
  }
}

