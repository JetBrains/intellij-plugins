package com.intellij.dts.zephyr.binding

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.concurrency.ThreadingAssertions
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import org.jetbrains.annotations.TestOnly

@Service(Service.Level.APP)
class DtsZephyrBundledBindings(scope: CoroutineScope) {
  companion object {
    @JvmStatic
    fun getInstance(): DtsZephyrBundledBindings = service()

    const val DEFAULT_BINDING = "default"
    const val FALLBACK_BINDING = "fallback"

    val NODE_BINDINGS = listOf("aliases", "chosen", "cpus", "memory", "reserved-memory")

    private const val BUNDLED_BINDINGS_PATH = "bindings"

    private val logger = Logger.getInstance(DtsZephyrBundledBindings::class.java)
  }

  private var bindings: Deferred<Map<String, DtsZephyrBinding>> = scope.async(start = CoroutineStart.LAZY) {
    getSource()?.let(::parseBundledBindings) ?: emptyMap()
  }

  @TestOnly
  suspend fun awaitInit(): Unit = bindings.join()

  @RequiresBackgroundThread
  fun getSource(): BindingSource? {
    ThreadingAssertions.assertBackgroundThread()

    val classLoader = DtsZephyrBundledBindings::class.java.classLoader

    val url = classLoader.getResource(BUNDLED_BINDINGS_PATH)
    if (url === null) {
      logger.error("failed to load bundled bindings folder url")
      return null
    }

    val dir = VfsUtil.findFileByURL(url)
    if (dir == null) {
      logger.error("failed to load bundled bindings folder")
      return null
    }

    return BindingSource(loadBundledBindings(dir), null)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  fun getBinding(name: String): DtsZephyrBinding? {
    return if (!bindings.isCompleted) {
      bindings.start()

      return null
    } else {
      bindings.getCompleted()[name]
    }
  }
}
