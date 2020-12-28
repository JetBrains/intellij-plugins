package org.jetbrains.idea.perforce

import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.FileStatusManager
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.idea.perforce.application.PerforceRepositoryLocation
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.connections.AbstractP4Connection
import org.jetbrains.idea.perforce.perforce.connections.P4ConfigFields
import org.junit.After
import org.junit.Before
import org.junit.Test

import static com.intellij.testFramework.UsefulTestCase.*
import static org.junit.Assert.assertEquals

abstract class PerforceIgnoredTest extends PerforceTestCase {
  List<String> myCommands

  @Override
  @Before
  void before() {
    super.before()
    myCommands = AbstractP4Connection.dumpCommands(myTestRootDisposable)
  }

  @Override
  @After
  void after() throws Exception {
    myCommands = null
    super.after()
  }

  @Test
  void testIgnoredFiles() throws Exception {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)

    ignoreFiles(System.lineSeparator() + "a.txt")
    VirtualFile a = createFileInCommand("a.txt", "")
    VirtualFile b = createFileInCommand("b.txt", "")
    refreshChanges()

    assertSameElements(getChangeListManager().getUnversionedFiles(), b)
    assertEquals(FileStatus.IGNORED, FileStatusManager.getInstance(myProject).getStatus(a))
  }

  @Test
  void "files with ellipsis in name"() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)

    def file = createFileInCommand("some...file.txt", "content")
    changeListManager.waitUntilRefreshed()

    assert !changeListManager.allChanges
    assert changeListManager.unversionedFiles as Set == [file] as Set
    assert !changeListManager.modifiedWithoutEditing
  }

  @Test
  void "automatic force refresh after p4ignore file changes"() {
    def customName = "customName"

    Map<String, String> env = new HashMap<>()
    env.put(P4ConfigFields.P4IGNORE.name, customName)
    AbstractP4Connection.setTestEnvironment(env, myTestRootDisposable)

    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)
    def p4config = createFileInCommand(TEST_P4CONFIG, createP4Config('test'))
    def p4ignore = createFileInCommand(customName, P4_IGNORE_NAME + System.lineSeparator() + customName + System.lineSeparator() + "a.txt")
    VirtualFile a = createFileInCommand("a.txt", "")
    VirtualFile b = createFileInCommand("b.txt", "")
    refreshChanges()

    assertSameElements(getChangeListManager().getUnversionedFiles(), b, p4config)
    assertEquals(FileStatus.IGNORED, FileStatusManager.getInstance(myProject).getStatus(a))

    editFileInCommand(p4ignore, P4_IGNORE_NAME)
    refreshChanges()
    assertSameElements(getChangeListManager().getUnversionedFiles(), a, b, p4ignore, p4config)
  }

  @Test
  void "test no incoming changes from ignored directory"() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)

    def idea = createDirInCommand(myWorkingCopyDir, ".idea")
    createFileInCommand("a.txt", "")
    addFile("a.txt")
    verify(runP4WithClient("sync", "//depot/...@0"))

    ignoreFiles(System.lineSeparator() + ".idea")
    submitDefaultList("initial")
    verify(runP4WithClient("sync", "//depot/...@0"))

    def provider = PerforceVcs.getInstance(myProject).getCommittedChangesProvider()
    assertOneElement(provider.getIncomingFiles(PerforceRepositoryLocation.create(myWorkingCopyDir, myProject)))
    assertEmpty(provider.getIncomingFiles(PerforceRepositoryLocation.create(idea, myProject)))
  }

}

class WithoutIgnoresCommandTest extends PerforceIgnoredTest {

}

class WithIgnoresCommandTest extends PerforceIgnoredTest {
  @Override
  protected String getPerforceVersion() {
    return "2016.2"
  }

  @Override
  @After
  void after() {
    try {
      assert !myCommands.find { it.contains('add -f') }
      assert !myCommands.find { it.contains('add -n') }
    }
    finally {
      super.after()
    }
  }
}
