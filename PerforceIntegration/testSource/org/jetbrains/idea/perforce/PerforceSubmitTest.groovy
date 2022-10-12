package org.jetbrains.idea.perforce

import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import org.jetbrains.idea.perforce.application.PerforceCheckinEnvironment
import org.jetbrains.idea.perforce.application.PerforceManager
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.PerforceChange
import org.junit.Assert
import org.junit.Test
class PerforceSubmitTest extends PerforceTestCase {
  @Override
  void before() throws Exception {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
  }

  @Test
  void testIncludeStdoutDiagnosticsOnSubmitFailure() throws VcsException {
    def file = createFileInCommand("a.txt", "aaa")
    submitDefaultList('initial')

    renameFileInCommand(file, 'b.txt')
    changeListManager.waitUntilRefreshed()
    assert singleChange

    try {
      def env = new PerforceCheckinEnvironment(myProject, PerforceVcs.getInstance(myProject))
      def job = new PerforceCheckinEnvironment.SubmitJob(env, connection)
      job.addChanges([PerforceChange.createOn('//depot/a.txt\t# move/delete', PerforceManager.getInstance(myProject).getClient(connection))])
      job.submit('comment', null)
      Assert.fail()
    }
    catch (VcsException e) {
      assert e.message.contains('needs tofile')
    }
  }

}
