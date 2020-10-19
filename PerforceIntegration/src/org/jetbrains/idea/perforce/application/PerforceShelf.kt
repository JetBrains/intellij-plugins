package org.jetbrains.idea.perforce.application

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.util.containers.MultiMap
import org.jetbrains.idea.perforce.perforce.connections.P4Connection

/**
 * @author peter
 */
class PerforceShelf(private val myProject: Project) {
  private val myCache = MultiMap.create<Pair<ConnectionKey, Long>, ShelvedChange>()

  fun getShelvedChanges(changeList: LocalChangeList): List<ShelvedChange> {
    val result = arrayListOf<ShelvedChange>()
    val map = PerforceNumberNameSynchronizer.getInstance(myProject).getAllNumbers(changeList.name)
    synchronized (myCache) {
      for ((changeListInConnection, shelvedChanges) in map.entrySet()) {
        shelvedChanges.flatMapTo(result) { myCache[changeListInConnection to it] }
      }
    }
    return result
  }

  fun clearShelf(): Unit = synchronized (myCache) { myCache.clear() }

  fun addShelvedChange(connection: P4Connection, changeList: Long, change: ShelvedChange) {
    synchronized (myCache) {
      myCache.putValue(connection.connectionKey to changeList, change)
    }
  }

  fun hasLocalChanges(key: ConnectionKey, number: Long): Boolean {
    val changes = synchronized (myCache) { myCache[key to number] }
    return changes.any { it.file != null }
  }
}
