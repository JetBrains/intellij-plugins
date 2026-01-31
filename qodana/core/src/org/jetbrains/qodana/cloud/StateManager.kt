package org.jetbrains.qodana.cloud

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Utility class for managing transitions between states
 * such as user state in [QodanaCloudStateService] and project link in [org.jetbrains.qodana.cloud.project.QodanaCloudProjectLinkService]
 *
 * Use [state] `StateFlow` for the current state, [exitedState] `SharedFlow` to observe events of exit from state
 */
class StateManager<T>(initialStateProvider: (StateManager<T>) -> T) {
  private val _state = MutableStateFlow(initialStateProvider.invoke(this))
  val state: StateFlow<T> = _state.asStateFlow()

  private val _exitedState = MutableSharedFlow<T>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val exitedState = _exitedState.asSharedFlow()

  fun <S : T> changeState(oldState: T, newState: S): S? {
    val successFullyUpdatedState = _state.compareAndSet(expect = oldState, update = newState)
    if (!successFullyUpdatedState) return null

    _exitedState.tryEmit(oldState)
    return newState
  }
}