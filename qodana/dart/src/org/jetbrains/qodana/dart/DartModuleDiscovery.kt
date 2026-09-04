package org.jetbrains.qodana.dart

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

internal data class DartModule(
  val path: Path,
  val type: DartModuleType,
)

internal enum class DartModuleType {
  DART,
  FLUTTER,
}

internal fun findDartModules(projectPath: Path): List<DartModule> = findDartModuleDirectories(projectPath).map { modulePath ->
  DartModule(modulePath, if (isFlutterModule(modulePath)) DartModuleType.FLUTTER else DartModuleType.DART)
}

internal fun findDartModuleDirectories(projectPath: Path): List<Path> {
  val moduleDirectories = ArrayList<Path>()
  Files.walkFileTree(projectPath, object : SimpleFileVisitor<Path>() {
    override fun preVisitDirectory(directory: Path, attributes: BasicFileAttributes): FileVisitResult {
      return if (directory != projectPath && directory.fileName.toString().startsWith('.')) {
        FileVisitResult.SKIP_SUBTREE
      }
      else {
        FileVisitResult.CONTINUE
      }
    }

    override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
      if (file.fileName.toString() == "pubspec.yaml") {
        moduleDirectories.add(file.parent)
      }
      return FileVisitResult.CONTINUE
    }

    override fun visitFileFailed(file: Path, exception: java.io.IOException): FileVisitResult = FileVisitResult.CONTINUE
  })
  return moduleDirectories.sorted()
}

private val FLUTTER_MODULE_DECLARATION = Regex("""^\s*(?:sdk\s*:\s*['"]?flutter['"]?\s*(?:#.*)?|flutter\s*:.*)$""")

internal fun isFlutterModule(moduleDirectory: Path): Boolean = runCatching {
  Files.readAllLines(moduleDirectory.resolve("pubspec.yaml")).any(FLUTTER_MODULE_DECLARATION::matches)
}.getOrDefault(false)
