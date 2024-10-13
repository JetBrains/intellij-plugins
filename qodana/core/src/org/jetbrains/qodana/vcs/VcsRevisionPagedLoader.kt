@file:OptIn(ExperimentalCoroutinesApi::class)

package org.jetbrains.qodana.vcs

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.coroutines.QodanaDispatchers

private const val SHARED_FLOW_REPLAY = 1

internal data class RevisionPage(
  val revisions: List<VcsRevision>
)

private data class RevisionPagesCollection(
  val pages: List<RevisionPage>,
  val isFinished: Boolean
) {
  val pagesCount: Int
    get() = pages.size
}

private val LOG = logger<VcsRevisionPagedLoader>()

/**
 * Provides the asynchronous retrieval of revisions in form of `Flow<RevisionPage>`, see [startRevisionPagesCalculation]
 *
 * Revisions pages are collected and computed in one coroutine and provided via one shared flow [allRevisionPagesSharedFlow].
 * Revisions are computed ONLY on demand (next revision page is computed only when the collector finished processing current page)
 *
 * See [org.jetbrains.qodana.VcsRevisionPagedLoaderTest]
 */
internal class VcsRevisionPagedLoader(
  scope: CoroutineScope,
  private val project: Project,
  private val vcsRevisionProvider: VcsRevisionProvider,
  private val pageSize: Int,
  private val maxPages: Int,
) {
  private val allRevisionPagesSharedFlow: SharedFlow<RevisionPagesCollection> = allRevisionPagesFlow()
    .flowOn(QodanaDispatchers.Default)
    .buffer(0) // by default shareIn has a buffer, we don't want that since we want to compute revisions only on demand
    .shareIn(scope, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = SHARED_FLOW_REPLAY)

  private fun allRevisionPagesFlow(): Flow<RevisionPagesCollection> {
    var pagesCache = RevisionPagesCollection(emptyList(), isFinished = false)

    return flow { // need to wrap to flow so that cache would be reused on new collection
      val pagesFromCache = pagesCache
      val innerFlow = flow {
        var isFinished = pagesFromCache.isFinished
        var alreadyFetchedRevisionsCount = pagesFromCache.pagesCount * pageSize
        while (!isFinished) {
          checkCanceled()
          val fetchedRevisions = vcsRevisionProvider.fetchHeadRevisions(
            project,
            fromCommitIdx = alreadyFetchedRevisionsCount,
            revisionsToFetchCount = pageSize
          )
          isFinished = fetchedRevisions.size < pageSize
          alreadyFetchedRevisionsCount += pageSize

          LOG.debug { "Obtained revisions page: $fetchedRevisions \n is finished: $isFinished" }

          emit(RevisionPage(fetchedRevisions) to isFinished)
        }
      }.scan(pagesFromCache) { previousPages: RevisionPagesCollection, (newPage, isFinished): Pair<RevisionPage, Boolean> ->
        val newPages = RevisionPagesCollection(
          previousPages.pages + newPage,
          isFinished
        )
        newPages.copy(isFinished = newPages.isFinished || newPages.pagesCount >= maxPages)
      }.onEach { pages ->
        pagesCache = pages
      }.transformWhile { pages ->
        LOG.debug { "Emitting revision pages: $pages" }
        emit(pages)
        !pages.isFinished
      }.transform { pages ->
        emit(pages)

        /**
         * hack: need to emit to shared flow more "fake" values to suspend until subscribers process actual value above,
         * otherwise we'll start computing new revisions while the subscribers are processing current value (we don't want that)
         * the fake values will be ignored by subscribers because of `distinctUntilChanged` in [startAllRevisionPagesCalculation]
         *
         * emit number of times matching [SHARED_FLOW_REPLAY]
         */
        repeat(SHARED_FLOW_REPLAY + 1) {
          emit(pages)
        }
      }
      emitAll(innerFlow)
    }
  }

  private fun startAllRevisionPagesCalculation(): Flow<RevisionPagesCollection> {
    return allRevisionPagesSharedFlow
      .distinctUntilChanged { old, new ->
        old === new
      }
      .transformWhile { pages ->
        emit(pages)
        !pages.isFinished
      }
  }

  fun startRevisionPagesCalculation(): Flow<RevisionPage> {
    var previousPagesCount = 0
    return startAllRevisionPagesCalculation()
      .map { pages ->
        val newPages = RevisionPagesCollection(
          pages.pages.drop(previousPagesCount),
          pages.isFinished
        )
        previousPagesCount = pages.pagesCount
        newPages
      }
      .flatMapConcat {
        it.pages.asFlow()
      }
      .filter {
        it.revisions.isNotEmpty()
      }
      .map {
        it.copy(revisions = it.revisions.sortedByDescending { it.date })
      }
      .cancellable()
  }
}