package org.jetbrains.qodana.coroutines

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.application
import com.intellij.util.lazyPub
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import org.jetbrains.annotations.BlockingExecutor
import kotlin.coroutines.CoroutineContext

private val LOG by lazyPub { logger<QodanaDispatchersProvider>() }

/**
 * Must be specified when launching any coroutine (except child ones) or switching context
 *
 * In production uses [QodanaDispatchersProviderImpl], in tests – [QodanaDispatchersProviderTestImpl]
 * (In tests it is convenient to execute all operations on one thread to avoid multithreading issues and ambiguity)
 */
val QodanaDispatchers get() = QodanaDispatchersProvider.getInstance()

@Suppress("PropertyName")
interface QodanaDispatchersProvider {
  companion object {
    fun getInstance() = if (application.isUnitTestMode) testImpl else productionImpl

    private val productionImpl by lazyPub { QodanaDispatchersProviderImpl() }
    private val testImpl get() = QodanaDispatchersProviderTestImpl.getInstance()
  }

  /**
   * See [Dispatchers.EDT]
   *
   * Be aware of modality logic: modality is passed to the dispatcher as a [CoroutineContext] element,
   * and behaviour of dispatcher depends on this modality (see [invokeLater]),
   * if there is no modality in current context, it passes the [ModalityState.nonModal] modality
   *
   * As an example of quite surprising behaviour, consider the following code:
   * ```
   * launch(QodanaDispatchers.UiAnyModality) {
   *   // block 1
   *   withContext(QodanaDispatchers.UiNonModal) {
   *     // block 2: not suspend code here
   *   }
   * }
   * ```
   * **BOTH  blocks 1 and 2 will execute even if there is a modal window active**
   * because actual dispatcher hasn't changed: it's still a [Dispatchers.EDT], so no there will be no separate dispatch of block 2
   */
  val Ui: CoroutineContext

  /**
   * See [Dispatchers.IO]
   */
  val IO: @BlockingExecutor CoroutineContext

  /**
   * See [Dispatchers.Default]
   *
   * - Used as a default dispatcher inside plugin
   * - For moving intense computing to background
   */
  val Default: CoroutineContext
}

internal val QodanaDispatchersProvider.UiAnyModality: CoroutineContext
  get() = Ui + ModalityState.any().asContextElement()

@Suppress("unused")
internal val QodanaDispatchersProvider.UiNonModal: CoroutineContext
  get() = Ui + ModalityState.nonModal().asContextElement()

private class QodanaDispatchersProviderImpl : QodanaDispatchersProvider {
  private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    LOG.error("Unhandled coroutine throwable", throwable)
  }

  override val Ui: CoroutineContext = Dispatchers.EDT + exceptionHandler

  override val IO: CoroutineContext = Dispatchers.IO + exceptionHandler

  override val Default: CoroutineContext = Dispatchers.Default + exceptionHandler
}

@Service(Service.Level.APP)
class QodanaDispatchersProviderTestImpl : QodanaDispatchersProvider {
  companion object {
    fun getInstance() = service<QodanaDispatchersProviderTestImpl>()
  }

  private val _handledExceptions: MutableSet<Throwable> = mutableSetOf()
  val handledExceptions: Set<Throwable> = _handledExceptions
  var exceptionsCount = 0
  private var isExceptionsAllowed = false

  private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    if (!isExceptionsAllowed) {
      exceptionsCount++
    } else {
      _handledExceptions.add(throwable)
    }
    LOG.warn("Unhandled coroutine throwable", throwable)
  }

  fun enableExceptionsHandling() {
    isExceptionsAllowed = true
  }

  fun disableExceptionHandling() {
    isExceptionsAllowed = false
  }

  fun resetHandledExceptions() {
    _handledExceptions.clear()
  }

  fun resetHandledExceptionsState() {
    resetHandledExceptions()
    exceptionsCount = 0
  }

  override val Ui: CoroutineContext get() = Dispatchers.EDT + exceptionHandler

  override val IO: CoroutineContext get() = Ui

  override val Default: CoroutineContext get() = Ui
}