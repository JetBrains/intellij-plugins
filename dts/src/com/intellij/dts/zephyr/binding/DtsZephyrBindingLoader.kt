package com.intellij.dts.zephyr.binding

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.util.concurrency.ThreadingAssertions
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlin.io.path.readText

data class BindingFile(val path: String?, val data: Map<*, *>)

private val yaml = Yaml(SafeConstructor(LoaderOptions()))
private val logger = Logger.getInstance("DtsZephyrBindingLoader")

private fun loadFileData(text: String): Map<*, *>? {
  try {
    return synchronized(yaml) {
      yaml.load(text)
    }
  }
  catch (e: Exception) {
    logger.debug("could not load yaml file", e)
  }

  return null
}

@RequiresBackgroundThread
fun loadExternalBindings(root: Path): Map<String, BindingFile> {
  ThreadingAssertions.assertBackgroundThread()

  if (!Files.isDirectory(root)) return emptyMap()

  val bindings = mutableMapOf<String, BindingFile>()

  try {
    Files.walk(root).use { stream ->
      for (file in stream) {
        if (Files.isDirectory(file) || file.extension != "yaml") continue

        loadFileData(file.readText())?.let {
          bindings[file.nameWithoutExtension] = BindingFile(file.pathString, it)
        }
      }
    }
  }
  catch (_: IOException) {
    logger.debug("could not walk binding directory: $root")
  }

  return bindings
}

@RequiresBackgroundThread
fun loadBundledBindings(dir: VirtualFile): Map<String, BindingFile> {
  ThreadingAssertions.assertBackgroundThread()

  val bindings = mutableMapOf<String, BindingFile>()

  for (file in dir.children) {
    if (file.isDirectory || file.extension != "yaml") continue

    val binding = loadFileData(file.readText())

    if (binding != null) {
      bindings[file.nameWithoutExtension] = BindingFile(null, binding)
    }
    else {
      logger.error("could not load bundled binding: $file")
    }
  }

  return bindings
}