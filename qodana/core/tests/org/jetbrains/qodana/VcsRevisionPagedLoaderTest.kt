package org.jetbrains.qodana

import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.vcs.RevisionPage
import org.jetbrains.qodana.vcs.VcsRevision
import org.jetbrains.qodana.vcs.VcsRevisionPagedLoader
import org.jetbrains.qodana.vcs.VcsRevisionProvider
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

private const val PAGE_SIZE = 5

class VcsRevisionPagedLoaderTest : QodanaPluginLightTestBase() {
  override fun runInDispatchThread(): Boolean = false

  private val revisionPage1 = revisionPage(from = 1, PAGE_SIZE)

  private val revisionPage2 = revisionPage(from = 6, PAGE_SIZE)

  private val lastRevisionPage3 = revisionPage(from = 11, PAGE_SIZE - 1)

  private fun revisionPage(from: Int, count: Int): RevisionPage {
    return RevisionPage(
      (from until from + count).map {
        VcsRevision(it.toString(), Instant.MAX)
      }
    )
  }

  fun `test vcs revision loader no loading without request`(): Unit = runDispatchingOnUi {
    val revisionProvider = VcsRevisionTestProvider(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    @Suppress("UNUSED_VARIABLE") val loader = vcsRevisionPagedLoader(revisionProvider)

    processPageMock()

    assertThat(revisionProvider.timesFetchWasCalled).isZero()
  }

  fun `test vcs revision loader load 1 page only`() = runDispatchingOnUi {
    val revisionProvider = VcsRevisionTestProvider(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    val loader = vcsRevisionPagedLoader(revisionProvider)

    val loadedPage = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .first()

    assertThat(loadedPage).isEqualTo(revisionPage1)
    assertThat(revisionProvider.timesFetchWasCalled).isOne()
  }

  fun `test vcs revision loader load 2 pages only`() = runDispatchingOnUi {
    val revisionProvider = VcsRevisionTestProvider(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    val loader = vcsRevisionPagedLoader(revisionProvider)

    val loadedPages = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .take(2)
      .toList()

    assertThat(loadedPages).isEqualTo(listOf(revisionPage1, revisionPage2))
    assertThat(revisionProvider.timesFetchWasCalled).isEqualTo(2)
  }

  fun `test vcs revision loader load all pages`() = runDispatchingOnUi {
    val revisionProvider = VcsRevisionTestProvider(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    val loader = vcsRevisionPagedLoader(revisionProvider)

    val loadedPages = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .toList()

    assertThat(loadedPages).isEqualTo(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    assertThat(revisionProvider.timesFetchWasCalled).isEqualTo(3)
  }

  fun `test vcs revision loader request one page twice, load only once`() = runDispatchingOnUi {
    val revisionProvider = VcsRevisionTestProvider(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    val loader = vcsRevisionPagedLoader(revisionProvider)

    val pageRequestedFirstTime = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .first()

    val pageRequestedSecondTime = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .first()

    assertThat(pageRequestedFirstTime).isEqualTo(revisionPage1)
    assertThat(pageRequestedSecondTime).isEqualTo(revisionPage1)
    assertThat(revisionProvider.timesFetchWasCalled).isOne()
  }

  fun `test vcs revision loader request to all pages, request one, compute each page once`() = runDispatchingOnUi {
    val revisionProvider = VcsRevisionTestProvider(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    val loader = vcsRevisionPagedLoader(revisionProvider)

    val loadedPagesOnFirstRequest = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .toList()

    val loadedPageOnSecondRequest = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .first()

    assertThat(loadedPagesOnFirstRequest).isEqualTo(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    assertThat(loadedPageOnSecondRequest).isEqualTo(revisionPage1)
    assertThat(revisionProvider.timesFetchWasCalled).isEqualTo(3)
  }

  fun `test vcs revision loader request one page, request two pages, compute each page once`() = runDispatchingOnUi {
    val revisionProvider = VcsRevisionTestProvider(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    val loader = vcsRevisionPagedLoader(revisionProvider)

    val loadedPageOnFirstRequest = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .first()

    val loadedPagesOnSecondRequest = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .take(2)
      .toList()


    assertThat(loadedPageOnFirstRequest).isEqualTo(revisionPage1)
    assertThat(loadedPagesOnSecondRequest).isEqualTo(listOf(revisionPage1, revisionPage2))
    assertThat(revisionProvider.timesFetchWasCalled).isEqualTo(2)
  }

  fun `test vcs revision loader with limit on max 2 pages`() = runDispatchingOnUi {
    val revisionProvider = VcsRevisionTestProvider(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    val loader = vcsRevisionPagedLoader(revisionProvider, maxPages = 2)

    val loadedPages = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .toList()

    assertThat(loadedPages).isEqualTo(listOf(revisionPage1, revisionPage2))
    assertThat(revisionProvider.timesFetchWasCalled).isEqualTo(2)
  }

  fun `test vcs revision loader with limit on max 2 pages request pages twice`() = runDispatchingOnUi {
    val revisionProvider = VcsRevisionTestProvider(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    val loader = vcsRevisionPagedLoader(revisionProvider, maxPages = 2)

    val loadedPagesOnFirstRequest = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .toList()

    val loadedPagesOnSecondRequest = loader
      .startRevisionPagesCalculation()
      .onEach {
        processPageMock()
      }
      .toList()

    assertThat(loadedPagesOnFirstRequest).isEqualTo(listOf(revisionPage1, revisionPage2))
    assertThat(loadedPagesOnSecondRequest).isEqualTo(listOf(revisionPage1, revisionPage2))
    assertThat(revisionProvider.timesFetchWasCalled).isEqualTo(2)
  }

  fun `test vcs revision loader two concurrent requests to load pages`() = runDispatchingOnUi {
    val revisionProvider = VcsRevisionTestProvider(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    val loader = vcsRevisionPagedLoader(revisionProvider)

    val loadedPagesOnFirstRequest = async(Dispatchers.Default) {
      loader
        .startRevisionPagesCalculation()
        .onEach {
          processPageMock()
        }
        .take(2)
        .toList()
    }

    val loadedPagesOnSecondRequest = async(Dispatchers.Default) {
      loader
        .startRevisionPagesCalculation()
        .onEach {
          processPageMock()
        }
        .toList()
    }

    assertThat(loadedPagesOnFirstRequest.await()).isEqualTo(listOf(revisionPage1, revisionPage2))
    assertThat(loadedPagesOnSecondRequest.await()).isEqualTo(listOf(revisionPage1, revisionPage2, lastRevisionPage3))
    assertThat(revisionProvider.timesFetchWasCalled).isEqualTo(3)
  }

  private class VcsRevisionTestProvider(val pages: List<RevisionPage>) : VcsRevisionProvider {
    private val _timesFetchWasCalled = AtomicInteger()
    val timesFetchWasCalled: Int
      get() = _timesFetchWasCalled.get()

    override fun revisionUpdatedFlow(project: Project): Flow<Unit> {
      return emptyFlow()
    }

    override suspend fun fetchHeadRevisions(project: Project, fromCommitIdx: Int, revisionsToFetchCount: Int): List<VcsRevision> {
      // SharingStarted.WhileSubscribed used in loader internally launches coroutine to cancel the flow collection when subscribers == 0
      // so we need to trigger this coroutine to get cancelled when subscribers == 0
      dispatchAllTasksOnUi()

      checkCanceled()
      return pages.getOrNull(_timesFetchWasCalled.getAndIncrement())?.revisions ?: emptyList()
    }
  }

  private fun vcsRevisionPagedLoader(revisionProvider: VcsRevisionTestProvider, maxPages: Int = 5): VcsRevisionPagedLoader {
    return VcsRevisionPagedLoader(
      scope,
      project,
      revisionProvider,
      pageSize = PAGE_SIZE,
      maxPages = maxPages,
    )
  }

  private suspend fun processPageMock() {
    delay((0.5).seconds)
  }
}