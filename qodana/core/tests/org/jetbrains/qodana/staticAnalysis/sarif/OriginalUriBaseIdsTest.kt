package org.jetbrains.qodana.staticAnalysis.sarif

import com.jetbrains.qodana.sarif.model.ArtifactLocation
import junit.framework.TestCase
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.withSystemProperty
import org.junit.Test
import java.nio.file.Path

class OriginalUriBaseIdsTest : QodanaTestCase() {

  @Test
  fun `empty property creates only SRCROOT`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "") {
      val originalUriBaseIds = createOriginalUriBaseIds()

      assertTrue(originalUriBaseIds.containsKey(SRCROOT_URI_BASE))
      assertFalse(originalUriBaseIds.containsKey(PROJECTROOT_URI_BASE))

      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation
      checkArtifactLocation(srcRoot, null, null, SRCROOT_DESCRIPTION)
    }
  }

  @Test
  fun `null property creates only SRCROOT`() = runTest {
    System.clearProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY)

    val originalUriBaseIds = createOriginalUriBaseIds()
    assertTrue(originalUriBaseIds.containsKey(SRCROOT_URI_BASE))
    assertFalse(originalUriBaseIds.containsKey(PROJECTROOT_URI_BASE))

    val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation
    checkArtifactLocation(srcRoot, null, null, SRCROOT_DESCRIPTION)
  }

  @Test
  fun `non-empty property creates both SRCROOT and PROJECTROOT`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "subdir/project") {
      val originalUriBaseIds = createOriginalUriBaseIds()

      assertTrue(originalUriBaseIds.containsKey(SRCROOT_URI_BASE))
      assertTrue(originalUriBaseIds.containsKey(PROJECTROOT_URI_BASE))

      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation
      checkArtifactLocation(
        srcRoot,
        "subdir/project/",
        PROJECTROOT_URI_BASE,
        SRCROOT_DESCRIPTION
      )

      val projectRoot = originalUriBaseIds[PROJECTROOT_URI_BASE] as ArtifactLocation
      checkArtifactLocation(projectRoot, null, null, PROJECTROOT_DESCRIPTION)
    }
  }

  @Test
  fun `path normalization adds trailing slash`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "subdir/project") {
      val originalUriBaseIds = createOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation

      assertTrue(srcRoot.uri.endsWith("/"))
      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `path normalization preserves existing trailing slash`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "subdir/project/") {
      val originalUriBaseIds = createOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation

      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `path normalization removes leading slash`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "/subdir/project") {
      val originalUriBaseIds = createOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation

      assertFalse(srcRoot.uri.startsWith("/"))
      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `path normalization converts backslashes to forward slashes`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "subdir\\project") {
      val originalUriBaseIds = createOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation

      assertFalse(srcRoot.uri.contains("\\"))
      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `path normalization handles complex windows path`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "\\subdir\\project\\") {
      val originalUriBaseIds = createOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation

      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `path normalization trims whitespace`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "  subdir/project  ") {
      val originalUriBaseIds = createOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation

      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `whitespace-only property creates only SRCROOT`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "   ") {
      val originalUriBaseIds = createOriginalUriBaseIds()

      assertTrue(originalUriBaseIds.containsKey(SRCROOT_URI_BASE))
      assertFalse(originalUriBaseIds.containsKey(PROJECTROOT_URI_BASE))

      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation
      assertNull(srcRoot.uri)
      assertNull(srcRoot.uriBaseId)
    }
  }

  @Test
  fun `open directory is added between project and source roots`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "project") {
      val originalUriBaseIds = createOriginalUriBaseIds()

      originalUriBaseIds.addOpenDirForRider(Path.of("solution"))

      val openDir = originalUriBaseIds[OPENDIR_URI_BASE] as ArtifactLocation
      checkArtifactLocation(openDir, "project/", PROJECTROOT_URI_BASE, OPENDIR_DESCRIPTION)
      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation
      checkArtifactLocation(srcRoot, "solution/", OPENDIR_URI_BASE, SRCROOT_DESCRIPTION)
    }
  }

  @Test
  fun `open directory is rooted when project is repository root`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "") {
      val originalUriBaseIds = createOriginalUriBaseIds()

      originalUriBaseIds.addOpenDirForRider(Path.of("solution"))

      val openDir = originalUriBaseIds[OPENDIR_URI_BASE] as ArtifactLocation
      checkArtifactLocation(openDir, null, null, OPENDIR_DESCRIPTION)
      val srcRoot = originalUriBaseIds[SRCROOT_URI_BASE] as ArtifactLocation
      checkArtifactLocation(srcRoot, "solution/", OPENDIR_URI_BASE, SRCROOT_DESCRIPTION)
    }
  }

  private fun checkArtifactLocation(artifactLocation: ArtifactLocation, expectedUri: String?, expectedUriBaseId: String?, expectedDescription: String?) {
    TestCase.assertEquals(expectedUri, artifactLocation.uri)
    TestCase.assertEquals(expectedUriBaseId, artifactLocation.uriBaseId)
    TestCase.assertEquals(expectedDescription, artifactLocation.description?.text)
  }
}
