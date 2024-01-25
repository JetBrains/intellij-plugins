package org.jetbrains.idea.perforce

import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import org.jetbrains.idea.perforce.perforce.connections.P4ParametersConnection
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager
import org.junit.Assert.assertEquals
import org.junit.Test

class Perforce2016Test : PerforceTestCase() {

  override fun getPerforceVersion(): String {
    return "2016.2"
  }

  @Test
  fun `test merge info from several P4CONFIG files`() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
    ignoreTestP4ConfigFiles()

    val dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    val dir2 = createDirInCommand(dir1, "dir2")
    createFileInCommand(myWorkingCopyDir, TEST_P4CONFIG, "P4PORT=localhost:$ourP4port")
    createFileInCommand(dir2, TEST_P4CONFIG, "P4CLIENT=test")

    setVcsMappings(createMapping(dir2))
    setUseP4Config()
    refreshChanges()
    assertChangesViewEmpty()

    val connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(dir2) as P4ParametersConnection
    assertEquals("test", connection.parameters.client)
    assertEquals("localhost:$ourP4port", connection.parameters.server)
  }
}