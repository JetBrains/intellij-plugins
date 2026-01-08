package org.jetbrains.qodana.inspectionKts.js

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ComponentManagerEx
import com.intellij.openapi.project.Project
import com.intellij.platform.util.coroutines.childScope
import com.intellij.testFramework.replaceService
import com.intellij.util.io.delete
import com.intellij.util.io.write
import org.jetbrains.qodana.inspectionKts.INSPECTIONS_KTS_DIRECTORY
import org.jetbrains.qodana.inspectionKts.KtsInspectionsManager
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories


suspend fun withInspectionKtsFile(
  projectPath: Path,
  filename: String,
  content: String,
  action: suspend () -> Unit
) {
  val inspectionKtsFile = projectPath.resolve(INSPECTIONS_KTS_DIRECTORY).resolve("$filename.inspection.kts")
  try {
    inspectionKtsFile.createParentDirectories()
    inspectionKtsFile.createFile()
    inspectionKtsFile.write(content)
    action.invoke()
  }
  finally {
    try {
      inspectionKtsFile.delete()
    }
    catch (_ : IOException) {
    }
  }
}

fun reinstantiateKtsServices(project: Project, testRootDisposable: Disposable){
  val scope = (project as ComponentManagerEx).getCoroutineScope().childScope("qdtest-inspections-services")

  val ktsInspectionManager = KtsInspectionsManager(project, scope.childScope("KtsInspectionsManager"))
  project.replaceService(KtsInspectionsManager::class.java, ktsInspectionManager, testRootDisposable)
}