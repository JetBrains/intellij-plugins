package com.intellij.protobuf.ide.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

internal class CustomDescriptorConfigurationTest : BasePlatformTestCase() {
  fun `test options declared in custom descriptor completion`() {
    myFixture.addFileToProject("/NewDescriptor.proto", """
      syntax = "proto2";
      
      message FileOptions {
        optional string CUSTOM_TEST_OPTION_1 = true;
        optional string CUSTOM_TEST_OPTION_2 = false;
      }
    """.trimIndent())
    PbProjectSettings.getInstance(myFixture.project).apply {
      isIncludeContentRoots = true
      // the file is located directly under the test content root -> must be detected
      descriptorPath = "/NewDescriptor.proto"
    }

    myFixture.configureByText("test.proto", """
      syntax = "proto2";
      
      option CUSTOM_<caret>
    """.trimIndent())
    myFixture.testCompletionVariants("test.proto", "CUSTOM_TEST_OPTION_1", "CUSTOM_TEST_OPTION_2")
  }
}