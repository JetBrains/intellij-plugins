package org.jetbrains.qodana.coroutines

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.BulkAwareDocumentListener
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.Path

fun documentChangesFlow(document: Document): Flow<Document> {
  return callbackFlow {
    val listenerDisposable = Disposer.newDisposable()
    val documentListener = object : BulkAwareDocumentListener.Simple {
      override fun afterDocumentChange(document: Document) {
        trySendBlocking(document)
      }
    }
    document.addDocumentListener(documentListener, listenerDisposable)
    awaitClose { Disposer.dispose(listenerDisposable) }
  }
}

fun vfsChangesFilterFlow(fileEventFilter: (VFileEvent) -> Boolean): Flow<Unit> {
  return vfsChangesMapFlow { events ->
    if (events.any(fileEventFilter)) Unit else null
  }.buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
}

fun <T> vfsChangesMapFlow(map: (List<VFileEvent>) -> T?): Flow<T> {
  return callbackFlow {
    val listenerDisposable = Disposer.newDisposable()
    val fileListener = object : AsyncFileListener {
      override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val values = map.invoke(events)
        if (values == null) return null

        return object : AsyncFileListener.ChangeApplier {
          override fun afterVfsChange() {
            trySendBlocking(values)
          }
        }
      }
    }
    VirtualFileManager.getInstance().addAsyncFileListener(fileListener, listenerDisposable)
    awaitClose { Disposer.dispose(listenerDisposable) }
  }
}

val VFileEvent.appearedFilePath: Path?
  get() {
    val pathString = when(this) {
      is VFileCreateEvent -> this.path
      is VFileMoveEvent -> this.newPath
      is VFilePropertyChangeEvent -> if (this.isRename) this.newPath else null
      else -> null
    }
    return pathString?.let { safeNioPath(it) }
  }

val VFileEvent.disappearedFilePath: Path?
  get() {
    val pathString = when(this) {
      is VFileDeleteEvent -> this.path
      is VFileMoveEvent -> this.oldPath
      is VFilePropertyChangeEvent -> if (this.isRename) this.oldPath else null
      else -> null
    }
    return pathString?.let { safeNioPath(it) }
  }

private fun safeNioPath(pathString: String): Path? {
  return try {
    Path(pathString)
  }
  catch (_: InvalidPathException) {
    null
  }
}

fun isInDumbModeFlow(project: Project): Flow<Boolean> {
  return merge(
    flow { emit(DumbService.isDumb(project)) },
    callbackFlow {
      val listenerDisposable = Disposer.newDisposable()
      val listener = object : DumbService.DumbModeListener {
        override fun enteredDumbMode() {
          trySendBlocking(true)
        }

        override fun exitDumbMode() {
          trySendBlocking(false)
        }
      }
      project.messageBus.connect(listenerDisposable).subscribe(DumbService.DUMB_MODE, listener)
      awaitClose { Disposer.dispose(listenerDisposable) }
    }
  )
}