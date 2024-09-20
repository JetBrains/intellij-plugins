package org.jetbrains.idea.perforce.config

import com.intellij.openapi.components.service
import com.intellij.vcs.test.VcsPlatformTest
import org.jetbrains.idea.perforce.perforce.connections.P4ConfigListener
import org.jetbrains.idea.perforce.perforce.connections.PerforceExternalConfigTracker
import org.jetbrains.idea.perforce.perforce.connections.PerforceWorkspaceConfigurator.Companion.P4CONFIG_NAME
import java.io.File
import java.util.*
import java.util.concurrent.CountDownLatch

class PerforceExternalConfigTrackerTest : VcsPlatformTest() {

  fun `test single config`() {
    test(configCount = 1, ModificationType.CREATE)
    test(configCount = 1, ModificationType.MODIFY)
    test(configCount = 1, ModificationType.DELETE)
  }

  fun `test multiple config`() {
    test(configCount = 3, ModificationType.CREATE)
    test(configCount = 3, ModificationType.MODIFY)
    test(configCount = 3, ModificationType.DELETE)
  }

  private fun test(configCount: Int, modificationType: ModificationType) {
    val configs = mutableListOf<File>()
    for (i in 0 until configCount) {
      val configDir = File(project.basePath, "${File.separator}$i${File.separator}")
      configDir.mkdirs()
      val config = configDir.resolve(P4CONFIG_NAME + i)
      if (modificationType == ModificationType.MODIFY || modificationType == ModificationType.DELETE) {
        config.createNewFile()
      }
      configs.add(config)
    }

    val configTracker = project.service<PerforceExternalConfigTracker>()
    val modificationCounter = ModificationCounter(configCount)
    project.messageBus.connect().subscribe(P4ConfigListener.TOPIC, modificationCounter)
    configTracker.startTracking()

    val configPaths = configs.map { it.path }.toSet()
    configTracker.addConfigsToTrack(configPaths)

    for (i in 0 until configCount) {
      val config = configs[i]
      when (modificationType) {
        ModificationType.CREATE -> config.writeText("config$i created")
        ModificationType.MODIFY -> config.writeText("config$i changed")
        ModificationType.DELETE -> config.delete()
      }
    }

    modificationCounter.countDownLatch.await()

    val modifiedConfigPaths = modificationCounter.getModifiedPaths()
    assertEquals(configs.size, modifiedConfigPaths.size)
    assertContainsElements(modifiedConfigPaths, configPaths)
  }

  private enum class ModificationType { CREATE, MODIFY, DELETE }

  private class ModificationCounter(expectedModificationCount: Int) : P4ConfigListener {
    private val modifiedConfigPaths: MutableSet<String> = Collections.synchronizedSet(hashSetOf<String>())
    val countDownLatch: CountDownLatch = CountDownLatch(expectedModificationCount)

    override fun notifyConfigChanged(configPath: String) {
      synchronized(modifiedConfigPaths) {
        modifiedConfigPaths.add(configPath)
        countDownLatch.countDown()
      }
    }

    fun getModifiedPaths() = synchronized(modifiedConfigPaths) { modifiedConfigPaths.toSet() }
  }
}
