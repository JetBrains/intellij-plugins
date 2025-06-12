package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Service
@State(name = "QodanaGithubNotificationPromoApplicationDismissalState", storages = [Storage(value = "qodana.xml")])
internal class QodanaGithubPromoNotificationApplicationDismissalState(): BaseQodanaGithubPromoNotificationDismissalState()

@Service(Service.Level.PROJECT)
@State(name = "QodanaGithubNotificationPromoProjectDismissalState", storages = [Storage(value = "qodana.xml")])
internal class QodanaGithubPromoNotificationProjectDismissalState(): BaseQodanaGithubPromoNotificationDismissalState()


internal open class BaseQodanaGithubPromoNotificationDismissalState():
  PersistentStateComponent<BaseQodanaGithubPromoNotificationDismissalState.State> {
  class State : BaseState() {
    var dismissed: Boolean by property(false)
  }

  private var state: State = State()

  private val _dismissedState: MutableStateFlow<Boolean> = MutableStateFlow(state.dismissed)
  val dismissedState: StateFlow<Boolean> = _dismissedState.asStateFlow()

  override fun getState(): BaseQodanaGithubPromoNotificationDismissalState.State {
    return state.apply { dismissed = _dismissedState.value }
  }

  override fun loadState(state: BaseQodanaGithubPromoNotificationDismissalState.State) {
    this.state = state
    _dismissedState.value = state.dismissed
  }

  fun disableNotification() {
    _dismissedState.value = true
  }
}