package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.RunManager
import com.intellij.internal.statistic.beans.MetricEvent
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.ProjectUsagesCollector
import com.intellij.openapi.project.Project

class PlatformioStateCollector : ProjectUsagesCollector() {

  private companion object {
    const val PLATFORMIO_ENV_PREFIX = "PLATFORMIO"
    const val PLATFORMIO_ENV_DIR_SUFFIX = "DIR"
  }

  private val GROUP = EventLogGroup("cidr.embedded.platformio.state", 1);

  private fun isPlatformioEnv(env: String) = env.startsWith(PLATFORMIO_ENV_PREFIX)
  private fun isDirEnv(env: String) = env.endsWith(PLATFORMIO_ENV_DIR_SUFFIX)

  private val BUILD_ENVS = listOf(
    "PLATFORMIO_BUILD_FLAGS",
    "PLATFORMIO_BUILD_SRC_FLAGS",
    "PLATFORMIO_BUILD_SRC_FILTER",
    "PLATFORMIO_EXTRA_SCRIPTS",
    "PLATFORMIO_DEFAULT_ENVS"
  )

  private val NUM_ENVS = EventFields.LogarithmicInt("num_envs")
  private val NUM_PIO_ENVS = EventFields.LimitedInt("num_pio_envs", 0..39)
  private val NUM_DIR_ENVS = EventFields.LimitedInt("num_dir_envs", 0..18)
  private val BUILD_ENVS_FIELD = EventFields.StringList("build_envs", BUILD_ENVS)

  /**
   * https://docs.platformio.org/en/latest/envvars.html
   */
  private val RUN_CONFIG_METRIC = GROUP.registerVarargEvent("run.config.env",
                                                     NUM_ENVS,
                                                     NUM_PIO_ENVS,
                                                     NUM_DIR_ENVS,
                                                     BUILD_ENVS_FIELD)

  override fun getGroup(): EventLogGroup = GROUP

  override fun getMetrics(project: Project): Set<MetricEvent> {
    val runManager = RunManager.getInstance(project)
    val runConfigurations = runManager.allConfigurationsList
      .filterIsInstance<PlatformioDebugConfiguration>()

    val runConfigMetrics = runConfigurations.map { runConfig ->
      RUN_CONFIG_METRIC.metric(NUM_ENVS with runConfig.envs.size,
                               NUM_PIO_ENVS with runConfig.envs.keys.count { isPlatformioEnv(it) },
                               NUM_DIR_ENVS with runConfig.envs.keys.count { isDirEnv(it) },
                               BUILD_ENVS_FIELD with runConfig.envs.keys.filter { it in BUILD_ENVS })
    }.toSet()

    return runConfigMetrics
  }
}