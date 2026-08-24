package org.jetbrains.qodana.staticAnalysis.sarif

import com.jetbrains.qodana.sarif.model.ArtifactLocation
import junit.framework.TestCase
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.withSystemProperty
import org.junit.Test

class OriginalUriBaseIdsTest : QodanaTestCase() {

  @Test
  fun `empty property creates only SRCROOT`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "") {
      val originalUriBaseIds = createRegularOriginalUriBaseIds()

      assertTrue(originalUriBaseIds.containsKey(OriginalUriBaseId.SRCROOT.uriBaseId))
      assertFalse(originalUriBaseIds.containsKey(OriginalUriBaseId.PROJECTROOT.uriBaseId))

      val srcRoot = originalUriBaseIds[OriginalUriBaseId.SRCROOT.uriBaseId] as ArtifactLocation
      checkArtifactLocation(srcRoot, null, null, OriginalUriBaseId.SRCROOT.description)
    }
  }

  @Test
  fun `null property creates only SRCROOT`() = runTest {
    System.clearProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY)

    val originalUriBaseIds = createRegularOriginalUriBaseIds()
    assertTrue(originalUriBaseIds.containsKey(OriginalUriBaseId.SRCROOT.uriBaseId))
    assertFalse(originalUriBaseIds.containsKey(OriginalUriBaseId.PROJECTROOT.uriBaseId))

    val srcRoot = originalUriBaseIds[OriginalUriBaseId.SRCROOT.uriBaseId] as ArtifactLocation
    checkArtifactLocation(srcRoot, null, null, OriginalUriBaseId.SRCROOT.description)
  }

  @Test
  fun `non-empty property creates both SRCROOT and PROJECTROOT`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "subdir/project") {
      val originalUriBaseIds = createRegularOriginalUriBaseIds()

      assertTrue(originalUriBaseIds.containsKey(OriginalUriBaseId.SRCROOT.uriBaseId))
      assertTrue(originalUriBaseIds.containsKey(OriginalUriBaseId.PROJECTROOT.uriBaseId))

      val srcRoot = originalUriBaseIds[OriginalUriBaseId.SRCROOT.uriBaseId] as ArtifactLocation
      checkArtifactLocation(
        srcRoot,
        "subdir/project/",
        OriginalUriBaseId.PROJECTROOT.uriBaseId,
        OriginalUriBaseId.SRCROOT.description
      )

      val projectRoot = originalUriBaseIds[OriginalUriBaseId.PROJECTROOT.uriBaseId] as ArtifactLocation
      checkArtifactLocation(projectRoot, null, null, OriginalUriBaseId.PROJECTROOT.description)
    }
  }

  @Test
  fun `path normalization adds trailing slash`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "subdir/project") {
      val originalUriBaseIds = createRegularOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[OriginalUriBaseId.SRCROOT.uriBaseId] as ArtifactLocation

      assertTrue(srcRoot.uri.endsWith("/"))
      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `path normalization preserves existing trailing slash`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "subdir/project/") {
      val originalUriBaseIds = createRegularOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[OriginalUriBaseId.SRCROOT.uriBaseId] as ArtifactLocation

      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `path normalization removes leading slash`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "/subdir/project") {
      val originalUriBaseIds = createRegularOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[OriginalUriBaseId.SRCROOT.uriBaseId] as ArtifactLocation

      assertFalse(srcRoot.uri.startsWith("/"))
      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `path normalization converts backslashes to forward slashes`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "subdir\\project") {
      val originalUriBaseIds = createRegularOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[OriginalUriBaseId.SRCROOT.uriBaseId] as ArtifactLocation

      assertFalse(srcRoot.uri.contains("\\"))
      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `path normalization handles complex windows path`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "\\subdir\\project\\") {
      val originalUriBaseIds = createRegularOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[OriginalUriBaseId.SRCROOT.uriBaseId] as ArtifactLocation

      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `path normalization trims whitespace`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "  subdir/project  ") {
      val originalUriBaseIds = createRegularOriginalUriBaseIds()
      val srcRoot = originalUriBaseIds[OriginalUriBaseId.SRCROOT.uriBaseId] as ArtifactLocation

      TestCase.assertEquals("subdir/project/", srcRoot.uri)
    }
  }

  @Test
  fun `whitespace-only property creates only SRCROOT`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "   ") {
      val originalUriBaseIds = createRegularOriginalUriBaseIds()

      assertTrue(originalUriBaseIds.containsKey(OriginalUriBaseId.SRCROOT.uriBaseId))
      assertFalse(originalUriBaseIds.containsKey(OriginalUriBaseId.PROJECTROOT.uriBaseId))

      val srcRoot = originalUriBaseIds[OriginalUriBaseId.SRCROOT.uriBaseId] as ArtifactLocation
      assertNull(srcRoot.uri)
      assertNull(srcRoot.uriBaseId)
    }
  }

  @Test
  fun `open directory is added between project and source roots`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "project") {
      val entries = createRepositoryRootEntries(OriginalUriBaseId.OPENDIR) +
                    OriginalUriBaseId.RIDER_SRCROOT.createEntry("solution")
      val originalUriBaseIds = createOriginalUriBaseIds(*entries)

      val openDir = originalUriBaseIds[OriginalUriBaseId.OPENDIR.uriBaseId] as ArtifactLocation
      checkArtifactLocation(openDir, "project/", OriginalUriBaseId.PROJECTROOT.uriBaseId, OriginalUriBaseId.OPENDIR.description)
      val srcRoot = originalUriBaseIds[OriginalUriBaseId.RIDER_SRCROOT.uriBaseId] as ArtifactLocation
      checkArtifactLocation(srcRoot, "solution/", OriginalUriBaseId.OPENDIR.uriBaseId, OriginalUriBaseId.RIDER_SRCROOT.description)
    }
  }

  @Test
  fun `open directory is rooted when project is repository root`() = runTest {
    withSystemProperty(PATH_FROM_PROJECT_ROOT_TO_PROJECT_DIR_PROPERTY, "") {
      val entries = createRepositoryRootEntries(OriginalUriBaseId.OPENDIR) +
                    OriginalUriBaseId.RIDER_SRCROOT.createEntry("solution")
      val originalUriBaseIds = createOriginalUriBaseIds(*entries)

      val openDir = originalUriBaseIds[OriginalUriBaseId.OPENDIR.uriBaseId] as ArtifactLocation
      checkArtifactLocation(openDir, null, null, OriginalUriBaseId.OPENDIR.description)
      val srcRoot = originalUriBaseIds[OriginalUriBaseId.RIDER_SRCROOT.uriBaseId] as ArtifactLocation
      checkArtifactLocation(srcRoot, "solution/", OriginalUriBaseId.OPENDIR.uriBaseId, OriginalUriBaseId.RIDER_SRCROOT.description)
    }
  }

  private fun checkArtifactLocation(artifactLocation: ArtifactLocation, expectedUri: String?, expectedUriBaseId: String?, expectedDescription: String?) {
    TestCase.assertEquals(expectedUri, artifactLocation.uri)
    TestCase.assertEquals(expectedUriBaseId, artifactLocation.uriBaseId)
    TestCase.assertEquals(expectedDescription, artifactLocation.description?.text)
  }

  private fun createRegularOriginalUriBaseIds() =
    createOriginalUriBaseIds(*createRepositoryRootEntries(OriginalUriBaseId.SRCROOT))
}
