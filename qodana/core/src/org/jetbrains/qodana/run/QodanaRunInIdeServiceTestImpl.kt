package org.jetbrains.qodana.run

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QodanaRunInIdeServiceTestImpl : QodanaRunInIdeService {
  private var instance: QodanaRunInIdeService? = null

  override val runState: StateFlow<QodanaRunState>
    get() = instance?.runState ?: MutableStateFlow(object : QodanaRunState.NotRunning {
      override fun run(runInIdeParameters: RunInIdeParameters): QodanaRunState.Running? = null
    })

  override val runsResults: StateFlow<Set<QodanaInIdeOutput>>
    get() = instance?.runsResults ?: MutableStateFlow(emptySet<QodanaInIdeOutput>()).asStateFlow()

  fun setInstance(instance: QodanaRunInIdeService, disposable: Disposable) {
    this.instance = instance
    Disposer.register(disposable) {
      this.instance = null
    }
  }
}