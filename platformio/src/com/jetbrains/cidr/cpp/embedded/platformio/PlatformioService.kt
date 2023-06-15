package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.*
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Tag
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioExecutionTarget
import com.jetbrains.cidr.cpp.embedded.platformio.project.builds.PlatformioBuildTarget
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioProjectResolvePolicy
import org.jetbrains.annotations.Nls
import java.util.concurrent.atomic.AtomicLong

@Service(Service.Level.PROJECT)
@State(name = "PlatformIO", storages = [Storage(StoragePathMacros.CACHE_FILE)])
class PlatformioService(val project: Project) : PersistentStateComponentWithModificationTracker<PlatformioState> {

  @Volatile
  var visibleActions: Set<String> = emptySet()
  private var state: PlatformioState = PlatformioState()
  private val stateModCounter = AtomicLong(1)
  var isUploadPortAuto: Boolean
    get() = state.isUploadPortAuto
    set(value) {
      state.isUploadPortAuto = value
      stateModCounter.incrementAndGet()
    }

  var uploadPort: String
    get() = state.uploadPort
    set(value) {
      state.uploadPort = value
      stateModCounter.incrementAndGet()
    }

  var verbose: Boolean
    get() = state.verbose
    set(value) {
      state.verbose = value
      stateModCounter.incrementAndGet()
    }

  var configJson: String?
    get() = state.configJson
    set(value) {
      state.configJson = value
      stateModCounter.incrementAndGet()
    }

  val metadataJson: Map<String, String?>
    get() = state.metadataJson

  fun cleanCache() {
    state.metadataJson.clear()
    state.configJson = null
    stateModCounter.incrementAndGet()
  }

  fun setMetadataJson(envName: String, json: String?) {
    state.metadataJson[envName] = json
    stateModCounter.incrementAndGet()
  }

  var targetExecutablePath: String? = null
  val buildConfigurationTargets: List<PlatformioBuildTarget> = listOf(PlatformioBuildTarget(project.name))
  var envs: List<PlatformioExecutionTarget> = emptyList()
    set(value) {
      stateModCounter.incrementAndGet()
      field = value
      state.envNames = value.map { it.id }
    }

  var iniFiles: Set<String> = emptySet()
  var svdPath: String? = null
  var targets: List<PlatformioTargetData> = emptyList()
    set(value) {
      field = value
      project.messageBus.syncPublisher(PLATFORMIO_UPDATES_TOPIC).targetsChanged(value)
    }

  override fun getState(): PlatformioState {
    return state
  }

  override fun loadState(state: PlatformioState) {
    this.state = state
    this.envs = state.envNames.map(::PlatformioExecutionTarget)
  }

  override fun getStateModificationCount(): Long = stateModCounter.get()
}

val PlatformioProjectResolvePolicyCleanCache: PlatformioProjectResolvePolicy = PlatformioProjectResolvePolicy(true)
val PlatformioProjectResolvePolicyPreserveCache: PlatformioProjectResolvePolicy = PlatformioProjectResolvePolicy(false)

fun refreshProject(project: Project, cleanCache: Boolean) {
  ApplicationManager.getApplication().invokeLater {
    WriteAction.run<Throwable> {
      val policy = if (cleanCache) PlatformioProjectResolvePolicyCleanCache else PlatformioProjectResolvePolicyPreserveCache
      ExternalSystemUtil.refreshProject(project.basePath!!, ImportSpecBuilder(project, ID).projectResolverPolicy(policy))
    }
  }
}

data class PlatformioTargetData(
  @NlsSafe val name: String,
  @Nls val title: String?,
  @Nls val description: String?,
  @Nls val group: String?
)

class PlatformioState {

  var envNames: List<String> = emptyList()

  @Tag
  var verbose: Boolean = false

  @Tag
  var configJson: String? = null

  @Tag
  var isUploadPortAuto: Boolean = true

  @Tag
  var uploadPort: String = if (SystemInfo.isWindows) "COM1" else "/dev/ttyUSB0"

  @MapAnnotation(keyAttributeName = "env")
  val metadataJson: MutableMap<String, String?> = mutableMapOf()

}

@Topic.ProjectLevel
val PLATFORMIO_UPDATES_TOPIC: Topic<PlatformioUpdatesNotifier> = Topic.create("custom name", PlatformioUpdatesNotifier::class.java)

interface PlatformioUpdatesNotifier {
  fun targetsChanged(newTargets: List<PlatformioTargetData>) {}
  fun reparseStarted() {}
  fun reparseSuccess() {}
  fun reparseFailed(pioStartFailed: Boolean) {}
}