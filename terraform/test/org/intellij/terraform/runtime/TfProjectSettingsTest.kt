// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TfProjectSettingsTest : BasePlatformTestCase() {

  fun testGetState() {
    val settings = TfProjectSettings()
    
    // Test default state
    val state = settings.getState()
    assertSame(settings, state)
    assertEquals("", state.toolPath)
    assertFalse(state.isFormattedBeforeCommit)
    
    // Test with modified properties
    settings.toolPath = "  /path/to/terraform  "
    settings.isFormattedBeforeCommit = true
    
    val modifiedState = settings.getState()
    assertSame(settings, modifiedState)
    assertEquals("/path/to/terraform", modifiedState.toolPath) // Should be trimmed
    assertTrue(modifiedState.isFormattedBeforeCommit)
  }
  
  fun testLoadState() {
    val settings = TfProjectSettings()
    val newState = TfProjectSettings()
    
    // Modify the new state
    newState.toolPath = "/path/to/terraform"
    newState.isFormattedBeforeCommit = true
    
    // Load the new state
    settings.loadState(newState)
    
    // Verify properties were copied
    assertEquals("/path/to/terraform", settings.toolPath)
    assertTrue(settings.isFormattedBeforeCommit)
  }
  
  fun testIgnoredTemplateCandidatePaths() {
    val settings = TfProjectSettings()
    
    // Test adding paths
    settings.addIgnoredTemplateCandidate("/path/to/file1")
    settings.addIgnoredTemplateCandidate("/path/to/file2")
    
    // Verify paths were added
    assertTrue(settings.isIgnoredTemplateCandidate("/path/to/file1"))
    assertTrue(settings.isIgnoredTemplateCandidate("/path/to/file2"))
    assertFalse(settings.isIgnoredTemplateCandidate("/path/to/file3"))
    
    // Test serialization and deserialization of ignored paths
    val newSettings = TfProjectSettings()
    newSettings.loadState(settings.getState())
    
    // Verify paths were preserved
    assertTrue(newSettings.isIgnoredTemplateCandidate("/path/to/file1"))
    assertTrue(newSettings.isIgnoredTemplateCandidate("/path/to/file2"))
    assertFalse(newSettings.isIgnoredTemplateCandidate("/path/to/file3"))
  }
  
  fun testSerializationWithMultipleOperations() {
    val settings = TfProjectSettings()
    
    // Add some paths
    settings.addIgnoredTemplateCandidate("/path/to/file1")
    settings.addIgnoredTemplateCandidate("/path/to/file2")
    
    // Create a new settings object and load the state
    val intermediateSettings = TfProjectSettings()
    intermediateSettings.loadState(settings.getState())
    
    // Add more paths to the intermediate settings
    intermediateSettings.addIgnoredTemplateCandidate("/path/to/file3")
    
    // Create a final settings object and load the state from intermediate
    val finalSettings = TfProjectSettings()
    finalSettings.loadState(intermediateSettings.getState())
    
    // Verify all paths were preserved through multiple serialization/deserialization cycles
    assertTrue(finalSettings.isIgnoredTemplateCandidate("/path/to/file1"))
    assertTrue(finalSettings.isIgnoredTemplateCandidate("/path/to/file2"))
    assertTrue(finalSettings.isIgnoredTemplateCandidate("/path/to/file3"))
    assertFalse(finalSettings.isIgnoredTemplateCandidate("/path/to/file4"))
  }
}