package org.jetbrains.idea.perforce.perforce.connections

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.util.awaitCancellationAndInvoke
import com.intellij.util.messages.Topic
import kotlinx.coroutines.*
import org.jetbrains.annotations.ApiStatus
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.io.path.name
import kotlin.io.path.pathString

@ApiStatus.Internal
@Service(Service.Level.PROJECT)
class PerforceExternalConfigTracker(private val project: Project, private val cs: CoroutineScope) {

  private val trackedConfigFiles = Collections.synchronizedSet(mutableSetOf<Pair<String, WatchKey>>())
  private var watchService: WatchService? = null
  private var trackJob: Job? = null

  init {
    cs.awaitCancellationAndInvoke { stopTracking() }
  }

  fun startTracking() {
    synchronized(trackedConfigFiles) {
      val runningTrackJob = trackJob
      if (runningTrackJob != null && runningTrackJob.isActive) {
        return
      }

      watchService = createWatchService()

      trackJob = cs.launch(Dispatchers.IO) {
        trackConfigs()
      }
    }
  }

  private fun stopTracking() {
    synchronized(trackedConfigFiles) {
      try {
        watchService?.close()
        val trackJob = trackJob ?: return
        if (trackJob.isActive) {
          trackJob.cancel()
        }
        watchService = null
        this.trackJob = null
      }
      catch (e: IOException) {
        LOG.warn("Unable to stop tracking", e)
      }
    }
  }

  fun addConfigsToTrack(configPaths: Set<String>) {
    processTracked { registeredConfigs ->
      val watchService = watchService ?: return@processTracked

      for (configPath in configPaths) {
        val configPath = configPath.toPathSafe() ?: continue
        val configDir = configPath.parent ?: continue
        val configName = configPath.name

        if (registeredConfigs.none { (name, directory) -> name == configName && directory == configDir }) {
          val watchKey = configDir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
          registeredConfigs.add(configName to watchKey)
        }
      }
    }
  }

  private fun createWatchService(): WatchService? {
    try {
      return FileSystems.getDefault().newWatchService()
    }
    catch (e: IOException) {
      LOG.warn("Unable to create watch service", e)
    }
    return null
  }

  private suspend fun trackConfigs() {
    var watchKey: WatchKey
    var watchService: WatchService = watchService ?: return
    try {
      while (true) {
        watchKey = watchService.take() ?: return

        coroutineContext.ensureActive()

        for (event in watchKey.pollEvents()) {
          coroutineContext.ensureActive()

          val context = event.context() ?: continue
          val fileName = context.toString()
          val configFileChanged = processTracked { tracked -> (fileName in tracked.map { it.first }.toSet()) }
          if (configFileChanged) {
            val configDir = watchKey.toPath()
            val configPath = "$configDir${File.separator}$fileName"
            notifyConfigChanged(project, configPath)
          }
        }
        watchKey.reset()
        watchService = this.watchService ?: break
      }
    }
    catch (_: ClosedWatchServiceException) {
    }
  }

  private fun notifyConfigChanged(project: Project, configPath: String) {
    BackgroundTaskUtil.syncPublisher(project, P4ConfigListener.TOPIC).notifyConfigChanged(configPath)
  }

  private fun String.toPathSafe(): Path? {
    try {
      return Path.of(this)
    }
    catch (e: InvalidPathException) {
      LOG.warn("Unable to track invalid path $this", e)
    }

    return null
  }

  private fun WatchKey.toPath(): String {
    return watchable().let { watchable -> (watchable as? Path)?.pathString ?: watchable.toString() }
  }

  private fun <R> processTracked(processor: (MutableSet<Pair<String, WatchKey>>) -> R): R {
    return synchronized(trackedConfigFiles) { processor(trackedConfigFiles) }
  }

  companion object {
    private val LOG = logger<PerforceExternalConfigTracker>()
  }
}

@ApiStatus.Internal
interface P4ConfigListener {

  fun notifyConfigChanged(configPath: String)

  companion object {
    @JvmField
    @Topic.ProjectLevel
    val TOPIC: Topic<P4ConfigListener> =
      Topic(P4ConfigListener::class.java, Topic.BroadcastDirection.NONE, true)
  }
}
