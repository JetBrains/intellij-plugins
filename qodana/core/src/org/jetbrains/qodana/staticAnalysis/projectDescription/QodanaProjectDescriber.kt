package org.jetbrains.qodana.staticAnalysis.projectDescription

import com.google.gson.GsonBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Implementations of this EP provide information of different aspects of project structure.
 * This information will be used in qodana user interface. There isn't strong restriction about information format.
 * All information provided by {@link #description(Project)} will be stored as {@link #id()}.json by GSON.
 * Also implementation could override {@link #describe(Path, Project)} to write description by itself.
 */
interface QodanaProjectDescriber {
  companion object {
    val LOG = Logger.getInstance(QodanaProjectDescriber::class.java)

    val EP_NAME: ExtensionPointName<QodanaProjectDescriber> = ExtensionPointName("org.intellij.qodana.projectDescriber")

    suspend fun runDescribers(path: Path, project: Project) = invokeAllDescribers { describe(path, project) }

    suspend fun runDescribersAfterWork(path: Path, project: Project) = invokeAllDescribers { describeAfterWork(path, project) }

    private suspend fun invokeAllDescribers(action: suspend QodanaProjectDescriber.() -> Unit) {
      coroutineScope {
        EP_NAME.extensionList.forEach {
          launch { it.action() }
        }
      }
    }
  }


  val id: String

  suspend fun description(project: Project): Any = Any()

  /**
   * @param path    - Path to store result
   * @param project - Project to describe
   */
  suspend fun describe(path: Path, project: Project) {
    writeDescription(path.resolve("$id.json"), description(project))
  }

  suspend fun describeAfterWork(path: Path, project: Project) {
    writeDescription(path.resolve("$id.json"), description(project))
  }

  suspend fun writeDescription(path: Path, description: Any) {
    val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    try {
      runInterruptible(StaticAnalysisDispatchers.IO) {
        Files.newBufferedWriter(path, StandardCharsets.UTF_8).use { writer ->
          gson.toJson(description, writer)
        }
      }
    }
    catch (e: IOException) {
      LOG.error("Writing description error. Path: $path", e)
    }
  }
}