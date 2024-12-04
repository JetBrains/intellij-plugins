package org.jetbrains.idea.perforce

import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import org.jetbrains.idea.perforce.application.PerforceCheckinEnvironment
import org.jetbrains.idea.perforce.application.PerforceManager
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.PerforceChange
import org.junit.Assert.*
import org.junit.Test

class PerforceSubmitTest : PerforceTestCase() {

  override fun before() {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
  }

  @Test
  fun testIncludeStdoutDiagnosticsOnSubmitFailure() {
    val file = createFileInCommand("a.txt", "aaa")
    refreshChanges()

    submitDefaultList("initial")

    renameFileInCommand(file, "b.txt")
    discardUnversionedCacheAndWaitFullRefresh()
    assertNotNull(singleChange)

    try {
      val env = PerforceCheckinEnvironment(myProject, PerforceVcs.getInstance(myProject))
      val job = env.SubmitJob(connection)
      job.addChanges(
        listOf(PerforceChange.createOn("//depot/a.txt\t# move/delete", PerforceManager.getInstance(myProject).getClient(connection))))
      job.submit("comment", null)
      fail()
    }
    catch (e: VcsException) {
      assertTrue(e.message.contains("needs tofile"))
    }
  }

}
