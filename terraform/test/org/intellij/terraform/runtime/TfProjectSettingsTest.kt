// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.openapi.util.JDOMUtil
import com.intellij.util.xmlb.XmlSerializer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal class TfProjectSettingsTest : BaseRunConfigurationTest() {

  fun testLoadingSimpleTfSettings() {
    val element = JDOMUtil.load(Paths.get("${getTestDataPath()}/tf-project-settings-simple.xml"))
    val settings = XmlSerializer.deserialize(element, TfProjectSettings::class.java)

    assertEquals("/path/to/terraform", settings.toolPath)
    assertTrue(settings.isFormattedBeforeCommit)
    assertTrue(settings.isIgnoredTemplateCandidate("/path/to/ignored_file1"))
    assertTrue(settings.isIgnoredTemplateCandidate("/path/to/ignored_file2"))
    assertFalse(settings.isIgnoredTemplateCandidate("/not/added_file"))
  }

  fun testSerializationAndDeserialization() {
    val settings = TfProjectSettings().apply {
      toolPath = "/opt/homebrew/bin/terraform"
      isFormattedBeforeCommit = false
      addIgnoredTemplateCandidate("/path/to/ignored_file1")
      addIgnoredTemplateCandidate("/path/to/ignored_file2")
    }

    val path: Path = Paths.get("${getTestDataPath()}/tf-project-settings.xml")
    val element = XmlSerializer.serialize(settings)
    Files.newBufferedWriter(path).use { writer ->
      JDOMUtil.writeElement(element, writer, "\n")
    }

    val newElement = JDOMUtil.load(path)
    val newSettings = XmlSerializer.deserialize(newElement, TfProjectSettings::class.java)

    assertEquals(settings.toolPath, newSettings.toolPath)
    assertEquals(settings.isFormattedBeforeCommit, newSettings.isFormattedBeforeCommit)
    assertTrue(newSettings.isIgnoredTemplateCandidate("/path/to/ignored_file1"))
    assertTrue(newSettings.isIgnoredTemplateCandidate("/path/to/ignored_file2"))
    assertFalse(newSettings.isIgnoredTemplateCandidate("/not/added_file"))
  }
}