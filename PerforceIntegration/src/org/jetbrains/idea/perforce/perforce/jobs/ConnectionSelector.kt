@file:JvmName("ConnectionSelector")
package org.jetbrains.idea.perforce.perforce.jobs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vcs.changes.ChangesUtil
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.util.Consumer
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.ConnectionKey
import org.jetbrains.idea.perforce.application.PerforceNumberNameSynchronizer
import org.jetbrains.idea.perforce.perforce.connections.P4Connection
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager
import java.util.*

fun getConnections(project: Project, list: LocalChangeList): Map<ConnectionKey, P4Connection> {
  val result = HashMap<ConnectionKey, P4Connection>()
  val files = ChangesUtil.getIoFilesFromChanges(list.changes);
  for (connection in PerforceConnectionManager.getInstance(project).allConnections.values) {
    val number = PerforceNumberNameSynchronizer.getInstance(project).getNumber(connection.connectionKey, list.name)
    if (number != null || files.any { connection.handlesFile(it) }) {
      result.put(connection.connectionKey, connection)
    }
  }

  return result
}

fun selectConnection(map: Map<ConnectionKey, P4Connection>, consumer: Consumer<ConnectionKey>) {
  val popup = object : BaseListPopupStep<ConnectionKey>(PerforceBundle.message("connection.select.title"), *map.keys.toTypedArray()) {
    override fun onChosen(selectedValue: ConnectionKey?, finalChoice: Boolean): PopupStep<*>? {
      consumer.consume(selectedValue)
      return PopupStep.FINAL_CHOICE
    }
  }
  JBPopupFactory.getInstance().createListPopup(popup).showInFocusCenter()
}
