package org.jetbrains.idea.perforce.changesBrowser

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings

@State(
  name = "RepositoryTabFilterSettings",
  storages = [Storage("repositoryTabFilterSettings.xml")]
)
@Service(Service.Level.PROJECT)
internal class PerforceChangeBrowserSettingsService : PersistentStateComponent<PerforceChangeBrowserSettingsService.State> {

  val settings: PerforceChangeBrowserSettings get() = stateToSettings()

  private var myState: State = State()

  data class State(
    var useClientFilter: Boolean = false,
    var client: String = "",
    var changeAfter: String = "",
    var changeBefore: String = "",
    var dateAfter: String? = "",
    var dateBefore: String? = "",
    var user: String = "",
    var useChangeAfterFilter: Boolean = false,
    var useChangeBeforeFilter: Boolean = false,
    var useDateAfterFilter: Boolean = false,
    var useDateBeforeFilter: Boolean = false,
    var useUserFilter: Boolean = false,
    var stopOnCopy: Boolean = false
  )

  override fun getState(): State = myState

  override fun loadState(state: State) {
    this.myState = state
  }

  fun saveSettings(settings: ChangeBrowserSettings) {
    myState = State(
      changeAfter = settings.CHANGE_AFTER,
      changeBefore = settings.CHANGE_BEFORE,
      dateAfter = settings.DATE_AFTER,
      dateBefore = settings.DATE_BEFORE,
      user = settings.USER,
      useChangeAfterFilter = settings.USE_CHANGE_AFTER_FILTER,
      useChangeBeforeFilter = settings.USE_CHANGE_BEFORE_FILTER,
      useDateAfterFilter = settings.USE_DATE_AFTER_FILTER,
      useDateBeforeFilter = settings.USE_DATE_BEFORE_FILTER,
      useUserFilter = settings.USE_USER_FILTER,
      stopOnCopy = settings.STOP_ON_COPY
    ).apply {
      (settings as? PerforceChangeBrowserSettings)?.also {
        useClientFilter = it.USE_CLIENT_FILTER
        client = it.CLIENT
      }
    }
  }

  private fun stateToSettings(): PerforceChangeBrowserSettings {
    return PerforceChangeBrowserSettings().apply {
      USE_CLIENT_FILTER = state.useClientFilter
      CLIENT = state.client
      CHANGE_AFTER = state.changeAfter
      CHANGE_BEFORE = state.changeBefore
      DATE_AFTER = state.dateAfter
      DATE_BEFORE = state.dateBefore
      USER = state.user
      USE_CHANGE_AFTER_FILTER = state.useChangeAfterFilter
      USE_CHANGE_BEFORE_FILTER = state.useChangeBeforeFilter
      USE_DATE_AFTER_FILTER = state.useDateAfterFilter
      USE_DATE_BEFORE_FILTER = state.useDateBeforeFilter
      USE_USER_FILTER = state.useUserFilter
      STOP_ON_COPY = state.stopOnCopy
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): PerforceChangeBrowserSettingsService =
      project.service<PerforceChangeBrowserSettingsService>()
  }
}
