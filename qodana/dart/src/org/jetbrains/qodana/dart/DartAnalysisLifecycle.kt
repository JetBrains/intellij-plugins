package org.jetbrains.qodana.dart

import java.nio.file.Path

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
