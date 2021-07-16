package org.jetbrains.idea.perforce

import com.intellij.idea.Bombed
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.testFramework.TestLoggerFactory
import com.intellij.util.CollectConsumer
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.UIUtil
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.jetbrains.idea.perforce.perforce.connections.AbstractP4Connection
import org.jetbrains.idea.perforce.perforce.connections.P4ConfigHelper
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager
import org.jetbrains.idea.perforce.perforce.login.LoginPerformerImpl
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager
import org.junit.Assume
import org.junit.Test

/**
 * @author peter
 */

class PerforceAuthenticationTest extends PerforceTestCase {
  @Override
  void before() throws Exception {
    super.before()
  }

  @Test
  void "test one authentication for several P4CONFIG roots"() {
    TestLoggerFactory.enableDebugLogging(myTestRootDisposable,
                                         "#org.jetbrains.idea.perforce.application.PerforceBaseInfoWorker",
                                         "#org.jetbrains.idea.perforce.application.PerforceInfoAndClient")


    int count = 4
    def dirs = (1..count).collect { createDirInCommand(myWorkingCopyDir, "dir$it") }

    List<String> commands = ContainerUtil.createEmptyCOWList()
    AbstractP4Connection.setCommandCallback(new CollectConsumer<String>(commands), myTestRootDisposable)
    AbstractP4Connection.setTestEnvironment([P4PORT:"localhost:" + ourP4port, P4USER:"test", P4CLIENT:"test"], myTestRootDisposable)
    changeListManager.waitUntilRefreshed()

    setUseP4Config()
    changeListManager.waitUntilRefreshed()

    setVcsMappings(dirs.collect { createMapping(it) })
    refreshChanges()
    assert commands.findAll { it.startsWith('login') }.size() == 0
    
    UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
    refreshChanges()
    assert commands.findAll { it.startsWith('login') }.size() == 1
  }

  @Test
  @Bombed(year = 2021, month = Calendar.AUGUST, day = 20, user = "AMPivovarov")
  void "test separate connections for different p4config files"() {
    def dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    def dir2 = createDirInCommand(myWorkingCopyDir, "dir2")
    setupTwoClients(dir1, dir2)

    def connections = PerforceConnectionManager.getInstance(myProject).allConnections.values() as List
    assert connections.size() == 2
    assert !connections[0].connectionKey.equals(connections[1].connectionKey)

    List<String> commands = ContainerUtil.createEmptyCOWList()
    AbstractP4Connection.setCommandCallback(new CollectConsumer<String>(commands), myTestRootDisposable)
    UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
    refreshChanges()
    assert commands.findAll { it.startsWith('login') }.size() == 2
  }
  
  @Test
  @Bombed(year = 2021, month = Calendar.AUGUST, day = 20, user = "AMPivovarov")
  void "test use default p4config file name when no P4CONFIG env variable is defined"() {
    assert !P4ConfigHelper.hasP4ConfigSettingInEnvironment()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)

    def config = createFileInCommand(TEST_P4CONFIG, createP4Config('test'))
    changeListManager.waitUntilRefreshed()
    
    setUseP4Config()
    UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
    refreshChanges()
    assert config == singleChange.virtualFile
  }

  @Test
  void "test custom p4config file name"() {
    Assume.assumeFalse("p4 set doesn't work on linux :(, so skipping PerforceAuthenticationTest.test custom p4config file name",
                       SystemInfo.isLinux)

    assert !P4ConfigHelper.hasP4ConfigSettingInEnvironment()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)

    def customName = "customName"
    withP4SetVariable('P4CONFIG', customName) {
      def config = createFileInCommand(customName, createP4Config('test'))
      addFile(customName, true) //Add explicitly. With p4 2015.1 and later clients, P4CONFIG files are ignored by default
      changeListManager.waitUntilRefreshed()

      setUseP4Config(customName)
      UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
      refreshChanges()
      assert config == singleChange.virtualFile
    }
  }

  @Test
  void "test do not use login command when that setting is off"() {
    List<String> commands = ContainerUtil.createEmptyCOWList()
    AbstractP4Connection.setCommandCallback(new CollectConsumer<String>(commands), myTestRootDisposable)
    UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
    assert commands.findAll { it.startsWith('login') }.size() == 1
    commands.clear()
    
    PerforceSettings.getSettings(myProject).USE_LOGIN = false
    UIUtil.invokeAndWaitIfNeeded { PerforceLoginManager.getInstance(myProject).checkAndRepairAll() }
    assert commands.findAll { it.startsWith('login') }.size() == 0
  }

  @Test
  void "test parse expiration ticket"() {
    def loginState = LoginPerformerImpl.parseExpirationTicket("User John ticket expires in 5 hours 55 minutes.")
    assert loginState.timeLeft == 21_300_000

    loginState = LoginPerformerImpl.parseExpirationTicket("User John ticket expires in 55 minutes.")
    assert loginState.timeLeft == 3_300_000

    loginState = LoginPerformerImpl.parseExpirationTicket("User John ticket expires in 22 hours 5 minutes.\n")
    assert loginState.timeLeft == 79_500_000
  }
  
}
