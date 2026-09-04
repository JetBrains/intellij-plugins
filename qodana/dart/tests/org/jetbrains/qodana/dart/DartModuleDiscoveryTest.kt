package org.jetbrains.qodana.dart

import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.util.io.NioFiles
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class DartModuleDiscoveryTest {
  @TempDir
  lateinit var tempDirectory: Path

  @Test
  fun `finds Dart modules while ignoring hidden directories`() {
    val monorepoPath = copyMiniMonorepo()

    assertEquals(
      listOf(
        monorepoPath.resolve("apps/app"),
        monorepoPath.resolve("packages/core"),
        monorepoPath.resolve("packages/core/nested_flutter_app"),
        monorepoPath.resolve("packages/flutter_app"),
      ),
      findDartModuleDirectories(monorepoPath),
    )
  }

  @Test
  fun `finds no modules in empty project`() {
    val emptyProject = Files.createDirectory(tempDirectory.resolve("empty-project"))

    assertTrue(findDartModuleDirectories(emptyProject).isEmpty())
  }

  @Test
  fun `finds no modules when only ignored directories exist`() {
    val project = tempDirectory.resolve("project")
    Files.createDirectories(project.resolve(".dart_tool/module"))
    Files.writeString(project.resolve(".dart_tool/module/pubspec.yaml"), "name: ignored")

    assertTrue(findDartModuleDirectories(project).isEmpty())
  }

  @Test
  fun `detects Flutter modules`() {
    val flutterModule = copyMiniMonorepo().resolve("packages/flutter_app")

    assertTrue(isFlutterModule(flutterModule))
    val modules = findDartModules(flutterModule)
    assertEquals(1, modules.size)
    assertEquals(DartModuleType.FLUTTER, modules[0].type)
  }

  @Test
  fun `returns false for malformed pubspec`() {
    assertFalse(isFlutterModule(createModule("malformed", "")))
  }

  @Test
  fun `returns false when pubspec is missing`() {
    assertFalse(isFlutterModule(Files.createDirectory(tempDirectory.resolve("no-pubspec"))))
  }

  @Test
  fun `returns false for non-Flutter pubspec`() {
    val module = createModule(
      "dart-only",
      """
      name: my_app
      environment:
        sdk: ">=2.12.0 <4.0.0"
      """.trimIndent(),
    )

    assertFalse(isFlutterModule(module))
  }

  @Test
  fun `detects a Flutter SDK dependency`() {
    val module = createModule(
      "flutter-app",
      """
      name: my_flutter_app
      dependencies:
        flutter:
          sdk: flutter
      """.trimIndent(),
    )

    assertTrue(isFlutterModule(module))
  }

  @Test
  fun `detects a Flutter section`() {
    val module = createModule(
      "flutter-section",
      """
      name: my_flutter_app
      flutter:
        uses-sdk: flutter
      """.trimIndent(),
    )

    assertTrue(isFlutterModule(module))
  }

  @Test
  fun `detects a Flutter test development dependency`() {
    val module = createModule(
      "dart-package-with-flutter-tests",
      """
      name: dart_package_with_flutter_tests
      dev_dependencies:
        flutter_test:
          sdk: flutter
      """.trimIndent(),
    )

    assertTrue(isFlutterModule(module))
  }

  private fun createModule(name: String, pubspec: String): Path {
    val module = Files.createDirectory(tempDirectory.resolve(name))
    Files.writeString(module.resolve("pubspec.yaml"), pubspec)
    return module
  }

  private fun copyMiniMonorepo(): Path {
    val source = Path.of(PluginPathManager.getPluginHomePath("qodana"), "dart", "test-data", "dart-analyze", "mini-monorepo")
    val target = tempDirectory.resolve("mini-monorepo")
    NioFiles.copyRecursively(source, target)
    return target
  }
}
