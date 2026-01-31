package org.jetbrains.idea.perforce

import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.TestLoggerFactory
import com.intellij.util.CollectConsumer
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.UIUtil
import org.jetbrains.idea.perforce.application.PerforceBaseInfoWorker
import org.jetbrains.idea.perforce.application.PerforceInfoAndClient
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.jetbrains.idea.perforce.perforce.connections.AbstractP4Connection
import org.jetbrains.idea.perforce.perforce.connections.P4EnvHelper
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager
import org.jetbrains.idea.perforce.perforce.login.LoginPerformerImpl
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assume
import org.junit.Test

class PerforceAuthenticationTest : PerforceTestCase() {

  override fun before() {
    super.before()
    unsetUseP4Config()
  }

  @Test
  fun `test one authentication for several P4CONFIG roots`() {
    TestLoggerFactory.enableDebugLogging(myTestRootDisposable, PerforceBaseInfoWorker::class.java, PerforceInfoAndClient::class.java)

    val count = 4
    val dirs = mutableListOf<VirtualFile>()
    for (i in 1..count) {
      dirs.add(createDirInCommand(myWorkingCopyDir, "dir$i"))
    }

    val commands: MutableList<String> = ContainerUtil.createConcurrentList()
    AbstractP4Connection.setCommandCallback(CollectConsumer(commands), myTestRootDisposable)
    AbstractP4Connection.setTestEnvironment(mapOf("P4PORT" to "localhost:$ourP4port", "P4USER" to "test", "P4CLIENT" to "test"),
                                            myTestRootDisposable)
    changeListManager.waitUntilRefreshed()

    setUseP4Config()
    changeListManager.waitUntilRefreshed()

    setVcsMappings(dirs.map { createMapping(it) })
    refreshChanges()
    assertEquals(0, commands.filter { it.startsWith("login") }.size)

    UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
    refreshChanges()
    assertEquals(1, commands.filter { it.startsWith("login") }.size)
  }

  @Test
  fun `test separate connections for different p4config files`() {
    val dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    val dir2 = createDirInCommand(myWorkingCopyDir, "dir2")
    setupTwoClients(dir1, dir2)

    val connections = PerforceConnectionManager.getInstance(myProject).allConnections.values.toList()
    assertEquals(2, connections.size)
    assertNotEquals(connections[0].connectionKey, connections[1].connectionKey)

    val commands: MutableList<String> = ContainerUtil.createConcurrentList()
    AbstractP4Connection.setCommandCallback(CollectConsumer(commands), myTestRootDisposable)
    UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
    refreshChanges()
    assertEquals(2, commands.filter { it.startsWith("login") }.size)
  }

  @Test
  fun `test use default p4config file name when no P4CONFIG env variable is defined`() {
    assertFalse(P4EnvHelper.hasP4ConfigSettingInEnvironment())
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)

    val config = createFileInCommand(TEST_P4CONFIG, createP4Config("test"))
    changeListManager.waitUntilRefreshed()

    setUseP4Config()
    UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
    refreshChanges()
    assertEquals(config, singleChange.virtualFile)
  }

  @Test
  fun `test custom p4config file name`() {
    Assume.assumeFalse("p4 set doesn't work on linux :(, so skipping PerforceAuthenticationTest.test custom p4config file name",
                       SystemInfo.isLinux)

    assertFalse(P4EnvHelper.hasP4ConfigSettingInEnvironment())
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)

    val customName = "customName"
    withP4SetVariable("P4CONFIG", customName) {
      val config = createFileInCommand(customName, createP4Config("test"))
      addFile(customName, true) //Add explicitly. With p4 2015.1 and later clients, P4CONFIG files are ignored by default
      changeListManager.waitUntilRefreshed()

      setUseP4Config(customName)
      UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
      refreshChanges()
      assertEquals(config, singleChange.virtualFile)
    }
  }

  @Test
  fun `test do not use login command when that setting is off`() {
    val commands: MutableList<String> = ContainerUtil.createConcurrentList()
    AbstractP4Connection.setCommandCallback(CollectConsumer(commands), myTestRootDisposable)
    UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
    assertEquals(1, commands.filter { it.startsWith("login") }.size)
    commands.clear()

    PerforceSettings.getSettings(myProject).USE_LOGIN = false
    UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
    assertEquals(0, commands.filter { it.startsWith("login") }.size)
  }

  @Test
  fun `test parse expiration ticket`() {
    var loginState = LoginPerformerImpl.parseExpirationTicket("User John ticket expires in 5 hours 55 minutes.")
    assertEquals(21_300_000, loginState.timeLeft)

    loginState = LoginPerformerImpl.parseExpirationTicket("User John ticket expires in 55 minutes.")
    assertEquals(3_300_000, loginState.timeLeft)

    loginState = LoginPerformerImpl.parseExpirationTicket("User John ticket expires in 22 hours 5 minutes.\n")
    assertEquals(79_500_000, loginState.timeLeft)
  }
}
