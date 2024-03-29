package com.intellij.dts.zephyr.binding

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.readText
import com.intellij.util.concurrency.ThreadingAssertions
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor

data class BindingFile(val path: String?, val data: Map<*, *>)

private val yaml = Yaml(SafeConstructor(LoaderOptions()))
private val logger = Logger.getInstance("DtsZephyrBindingLoader")

private fun loadFileData(file: VirtualFile): Map<*, *>? {
  try {
    return synchronized(yaml) {
      yaml.load(file.readText())
    }
  }
  catch (e: Exception) {
    logger.debug("could not load yaml file", e)
  }

  return null
}

@RequiresBackgroundThread
fun loadExternalBindings(root: VirtualFile): Map<String, BindingFile> {
  ThreadingAssertions.assertBackgroundThread()

  val bindings = mutableMapOf<String, BindingFile>()

  val visitor = object : VirtualFileVisitor<Any>() {
    override fun visitFile(file: VirtualFile): Boolean {
      if (file.isDirectory || file.extension != "yaml") return true

      loadFileData(file)?.let {
        bindings[file.nameWithoutExtension] = BindingFile(file.path, it)
      }

      return true
    }
  }
  VfsUtilCore.visitChildrenRecursively(root, visitor)

  return bindings
}

@RequiresBackgroundThread
fun loadBundledBindings(dir: VirtualFile): Map<String, BindingFile> {
  ThreadingAssertions.assertBackgroundThread()

  val bindings = mutableMapOf<String, BindingFile>()

  for (file in dir.children) {
    if (file.isDirectory || file.extension != "yaml") continue

    val binding = loadFileData(file)

    if (binding != null) {
      bindings[file.nameWithoutExtension] = BindingFile(null, binding)
    }
    else {
      logger.error("could not load bundled binding: $file")
    }
  }

  return bindings
}