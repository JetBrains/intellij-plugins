package org.jetbrains.qodana.staticAnalysis.testFramework

import com.intellij.util.io.delete
import com.intellij.util.io.write
import org.jetbrains.qodana.inspectionKts.INSPECTIONS_KTS_DIRECTORY
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