package org.jetbrains.qodana.cloud

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlin.time.Duration

/**
 * Container with mechanism of refreshing stored value,
 * can be seen as a cache with support of background refreshing
 *
 * Use [refreshLoop] to start process of background refreshing with [refreshDelay],
 * [refreshManually] to do a refresh "by hands",
 * [propertyState] state flow to retrieve and observe current property value
 */
class RefreshableProperty<T>(
  private val refreshDelay: Duration,
  initialValue: T? = null,
  private val compute: suspend (T?) -> T
) {
  private val _propertyState = MutableStateFlow(PropertyState(initialValue, isRefreshing = false))
  val propertyState = _propertyState.asStateFlow()

  data class PropertyState<T>(
    val lastLoadedValue: T?,
    val isRefreshing: Boolean
  )

  private val propertyRequests = Channel<PropertyRequest<T>>()

  private sealed class PropertyRequest<T>(val isCancellable: Boolean) {
    val taskDeferred = CompletableDeferred<Deferred<PropertyState<T>>>()

    class Compute<T>(isCancellable: Boolean, val transform: (T?) -> T? = { it }) : PropertyRequest<T>(isCancellable)

    class Set<T>(val value: PropertyState<T>) : PropertyRequest<T>(isCancellable = false)
  }

  suspend fun startRequestsProcessing() {
    coroutineScope {
      // sort of "conditional collectLatest"
      var isLastTaskCancellable = false
      var lastTask: Job? = null
      propertyRequests.consumeAsFlow().collect { propertyRequest ->
        if (isLastTaskCancellable) {
          lastTask?.cancel()
        }
        lastTask?.join()
        val currentTask = async {
          when(propertyRequest) {
            is PropertyRequest.Compute -> {
              val lastLoadedState = _propertyState.getAndUpdate { it.copy(isRefreshing = true) }
              var finalState = lastLoadedState
              try {
                val computedValue = compute.invoke(lastLoadedState.lastLoadedValue)
                finalState = PropertyState(propertyRequest.transform.invoke(computedValue), isRefreshing = false)
                finalState
              }
              finally {
                _propertyState.value = finalState
              }
            }
            is PropertyRequest.Set -> {
              val finalState = propertyRequest.value
              _propertyState.value = propertyRequest.value
              finalState
            }
          }
        }
        isLastTaskCancellable = propertyRequest.isCancellable
        lastTask = currentTask
        propertyRequest.taskDeferred.complete(currentTask)
      }
    }
  }

  suspend fun refreshManually(transform: (T?) -> T? = { it }): PropertyState<T> {
    val computeRequest = PropertyRequest.Compute(isCancellable = false, transform)
    propertyRequests.send(computeRequest)
    val task = computeRequest.taskDeferred.await()
    return try {
      task.await()
    } catch (ce : CancellationException) {
      task.cancel()
      throw ce
    }
  }

  suspend fun setValue(value: PropertyState<T>) {
    val setRequest = PropertyRequest.Set(value)
    propertyRequests.send(setRequest)
    setRequest.taskDeferred.await().join()
  }

  suspend fun refreshLoop() {
    while (true) {
      val request = PropertyRequest.Compute<T>(isCancellable = true)
      propertyRequests.send(request)
      request.taskDeferred.await().join()
      delay(refreshDelay)
    }
  }
}