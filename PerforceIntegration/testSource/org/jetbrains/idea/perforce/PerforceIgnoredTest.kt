package org.jetbrains.idea.perforce

import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.FileStatusManager
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vcs.changes.unversionedFiles
import org.jetbrains.idea.perforce.application.PerforceRepositoryLocation
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.connections.AbstractP4Connection
import org.jetbrains.idea.perforce.perforce.connections.P4ConfigFields
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

abstract class PerforceIgnoredTest : PerforceTestCase() {

  var myCommands: MutableList<String>? = null

  override fun before() {
    super.before()
    myCommands = AbstractP4Connection.dumpCommands(myTestRootDisposable)
  }

  override fun after() {
    myCommands = null
    super.after()
  }

  @Test  fun testIgnoredFiles() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)

    ignoreFiles(System.lineSeparator() + "a.txt")
    val a = createFileInCommand("a.txt", "")
    val b = createFileInCommand("b.txt", "")
    refreshChanges()

    assertThat(changeListManager.unversionedFiles).containsExactlyInAnyOrder(b)
    assertEquals(FileStatus.IGNORED, FileStatusManager.getInstance(myProject).getStatus(a))
  }

  @Test  fun `files with ellipsis in name`() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)

    val file = createFileInCommand("some...file.txt", "content")
    refreshChanges()

    assertTrue(changeListManager.allChanges.isEmpty())
    assertTrue(changeListManager.unversionedFiles.toSet() == setOf(file))
    assertTrue(changeListManager.modifiedWithoutEditing.isEmpty())
  }

  @Test  fun `automatic force refresh after p4ignore file changes`() {
    val customName = "customName"

    val env = HashMap<String, String>()
    env[P4ConfigFields.P4IGNORE.name] = customName
    AbstractP4Connection.setTestEnvironment(env, myTestRootDisposable)

    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)
    val p4config = createFileInCommand(TEST_P4CONFIG, createP4Config("test"))
    val p4ignore = createFileInCommand(customName, P4_IGNORE_NAME + System.lineSeparator() + customName + System.lineSeparator() + "a.txt")
    val a = createFileInCommand("a.txt", "")
    val b = createFileInCommand("b.txt", "")
    refreshChanges()

    assertThat(changeListManager.unversionedFiles).containsExactlyInAnyOrder(b, p4config)
    assertEquals(FileStatus.IGNORED, FileStatusManager.getInstance(myProject).getStatus(a))

    editFileInCommand(p4ignore, P4_IGNORE_NAME)
    discardUnversionedCache()
    changeListManager.waitUntilRefreshed()
    assertThat(changeListManager.unversionedFiles).containsExactlyInAnyOrder(a, b, p4ignore, p4config)
  }

  @Test  fun `test no incoming changes from ignored directory`() {
    assumeTrue(Registry.`is`("p4.use.p4.sync.for.incoming.files"))
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)

    val idea = createDirInCommand(myWorkingCopyDir, ".idea")
    createFileInCommand("a.txt", "")
    addFile("a.txt")
    verify(runP4WithClient("sync", "//depot/...@0"))

    ignoreFiles(System.lineSeparator() + ".idea")
    submitDefaultList("initial")
    verify(runP4WithClient("sync", "//depot/...@0"))

    val provider = PerforceVcs.getInstance(myProject).committedChangesProvider
    assertThat(provider.getIncomingFiles(PerforceRepositoryLocation.create(myWorkingCopyDir, myProject))!!).hasSize(1)
    assertTrue(provider.getIncomingFiles(PerforceRepositoryLocation.create(idea, myProject))!!.isEmpty())
  }

}

class WithoutIgnoresCommandTest : PerforceIgnoredTest()

class WithIgnoresCommandTest : PerforceIgnoredTest() {

  override fun getPerforceVersion(): String {
    return "2016.2"
  }

  override fun after() {
    try {
      assertNull(myCommands!!.find { it.contains("add -f") })
      assertNull(myCommands!!.find { it.contains("add -n") })
    }
    finally {
      super.after()
    }
  }
}
