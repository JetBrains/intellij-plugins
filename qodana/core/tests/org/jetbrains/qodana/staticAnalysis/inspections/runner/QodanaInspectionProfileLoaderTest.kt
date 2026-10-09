package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.InspectionApplicationException
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.junit.Assert
import org.junit.Test
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

class QodanaInspectionProfileLoaderTest : QodanaTestCase() {
  @Test
  fun `malformed yaml profile is a configuration error`() {
    val profileFile = createTempFile(suffix = ".yaml")
    try {
      profileFile.writeText("inspections: [unclosed")

      val e = Assert.assertThrows(QodanaConfigurationException::class.java) {
        QodanaInspectionProfileLoader(project).loadProfileByPath(profileFile.toString())
      }

      assertTrue(e.message!!, e.message!!.startsWith("Parse error in '$profileFile'"))
      assertTrue(e.cause is InspectionApplicationException)
    }
    finally {
      profileFile.deleteIfExists()
    }
  }
}
