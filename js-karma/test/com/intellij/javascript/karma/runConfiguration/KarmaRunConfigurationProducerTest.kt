// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.runConfiguration

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.javascript.karma.execution.KarmaRunConfiguration
import com.intellij.javascript.karma.execution.KarmaRunSettings
import com.intellij.javascript.karma.runConfiguration.infrastructure.KarmaRunConfigurationRunner
import com.intellij.javascript.karma.scope.KarmaScopeKind
import com.intellij.javascript.testFramework.runConfigurations.JsTestsRunConfigurationProducerTest
import com.intellij.javascript.testFramework.runConfigurations.queries.FileQuery

class KarmaRunConfigurationProducerTest:
  JsTestsRunConfigurationProducerTest<KarmaRunConfigurationProducerTest.KarmaSettingsFixture, KarmaRunConfiguration, KarmaRunSettings.Builder>()
{
  override fun getBasePath() = "/contrib/js-karma/testData/runConfiguration/configurationProducer"

  override fun getRunConfigurationClass(): Class<*>? = KarmaRunConfiguration::class.java

  override fun createRunConfigurationRunner(): KarmaRunConfigurationRunner = KarmaRunConfigurationRunner()

  // Jasmine
  fun `test forFile`() {
    assertDirOrFileRunConfigurationSettings(
      "Should have a suggest for file with tests",
      "src/user.spec.js",
      KarmaSettingsFixture(KarmaScopeKind.TEST_FILE, "src/user.spec.js", "karma.conf.js", workingDir = "")
    )

    assertDirOrFileRunConfigurationSettings(
      "Shouldn't have a suggest for file without tests",
      "src/just-a-file.js"
    )
  }

  fun `test guttersInFile`() {
    val fileQuery = FileQuery("src/user.spec.js")
    val baseSettingsFixture = KarmaSettingsFixture(KarmaScopeKind.SUITE, "src/user.spec.js", "karma.conf.js", workingDir = "")

    assertGuttersCount(fileQuery.filePath, 5)

    assertGutterRunConfigurationSettings(
      fileQuery.forLine(1),
      baseSettingsFixture.forSuite("user")
    )

    assertGutterRunConfigurationSettings(
      fileQuery.forLine(2),
      baseSettingsFixture.forTest("user", "should be tested")
    )

    assertGutterRunConfigurationSettings(
      fileQuery.forLine(6),
      baseSettingsFixture.forSuite("user", "subsuite")
    )

    assertGutterRunConfigurationSettings(
      "No gutter for a hook",
      fileQuery.forLine(7)
    )

    assertGutterRunConfigurationSettings(
      fileQuery.forLine(8),
      baseSettingsFixture.forTest("user", "subsuite", "test in subsuite")
    )

    assertGutterRunConfigurationSettings(
      fileQuery.forLine(14),
      baseSettingsFixture.forTest("test without suite")
    )
  }

  fun `test guttersInFileWithoutTestName`() {
    val fileQuery = FileQuery("src/file-with-some-name.js")
    val baseSettingsFixture = KarmaSettingsFixture(KarmaScopeKind.SUITE, "src/file-with-some-name.js", "karma.conf.js", workingDir = "")

    assertGuttersCount(fileQuery.filePath, 2)

    assertGutterRunConfigurationSettings(
      fileQuery.forLine(1),
      baseSettingsFixture.forSuite("user")
    )

    assertGutterRunConfigurationSettings(
      fileQuery.forLine(2),
      baseSettingsFixture.forTest("user", "should be tested")
    )
  }

  fun `test forPsiElements`() {
    val fileQuery = FileQuery("src/user.spec.js")
    val baseSettingsFixture = KarmaSettingsFixture(KarmaScopeKind.SUITE, "src/user.spec.js", "karma.conf.js", workingDir = "")

    asserPsiElementInFileRunConfigurationSettings(
      "Top level suite",
      fileQuery.forPsiElement("describe", 1, 7),
      baseSettingsFixture.forSuite("user")
    )

    asserPsiElementInFileRunConfigurationSettings(
      "Test in a suite",
      fileQuery.forPsiElement("'should be tested'", 2, 8),
      baseSettingsFixture.forTest("user", "should be tested")
    )

    asserPsiElementInFileRunConfigurationSettings(
      "Subsuite",
      fileQuery.forPsiElement("console", 7, 23),
      baseSettingsFixture.forSuite("user", "subsuite")
    )

    asserPsiElementInFileRunConfigurationSettings(
      "Test in subsuite",
      fileQuery.forPsiElement("it", 8, 6),
      baseSettingsFixture.forTest("user", "subsuite", "test in subsuite")
    )

    asserPsiElementInFileRunConfigurationSettings(
      "Test in subsuite",
      fileQuery.forPsiElement("expect", 15, 5),
      baseSettingsFixture.forTest("test without suite")
    )
  }

  // Mocha TDD UI
  fun `test forFileMochaTdd`() {
    assertDirOrFileRunConfigurationSettings(
      "Should have a suggest for file with tests",
      "src/array.spec.js",
      KarmaSettingsFixture(KarmaScopeKind.TEST_FILE, "src/array.spec.js", "karma.conf.js", workingDir = "")
    )

    assertDirOrFileRunConfigurationSettings(
      "Shouldn't have a suggest for file without tests",
      "src/just-a-file.js"
    )
  }

  fun `test guttersInFileMochaTdd`() {
    val fileQuery = FileQuery("src/array.spec.js")
    val baseSettingsFixture = KarmaSettingsFixture(KarmaScopeKind.SUITE, "src/array.spec.js", "karma.conf.js", workingDir = "")

    assertGuttersCount(fileQuery.filePath, 3)

    assertGutterRunConfigurationSettings(
      fileQuery.forLine(1),
      baseSettingsFixture.forSuite("Top level suite")
    )
    assertGutterRunConfigurationSettings(
      "No configuration for setup hook",
      fileQuery.forLine(2)
    )
    assertGutterRunConfigurationSettings(
      fileQuery.forLine(3),
      baseSettingsFixture.forSuite("Top level suite", "Subsuite")
    )
    assertGutterRunConfigurationSettings(
      fileQuery.forLine(4),
      baseSettingsFixture.forTest("Top level suite", "Subsuite", "Test in subsuite")
    )
  }

  fun `test forPsiElementsMochaTdd`() {
    val fileQuery = FileQuery("src/array.spec.js")
    val baseSettingsFixture = KarmaSettingsFixture(KarmaScopeKind.SUITE, "src/array.spec.js", "karma.conf.js", workingDir = "")

    asserPsiElementInFileRunConfigurationSettings(
      "Top level suite",
      fileQuery.forPsiElement("suite", 1, 3),
      baseSettingsFixture.forSuite("Top level suite")
    )
    asserPsiElementInFileRunConfigurationSettings(
      "Top level suite from a hook",
      fileQuery.forPsiElement("=>", 2, 13),
      baseSettingsFixture.forSuite("Top level suite")
    )
    asserPsiElementInFileRunConfigurationSettings(
      "Subsuite",
      fileQuery.forPsiElement("'Subsuite'", 3, 11),
      baseSettingsFixture.forSuite("Top level suite", "Subsuite")
    )
    asserPsiElementInFileRunConfigurationSettings(
      "Test in subsuite",
      fileQuery.forPsiElement("test", 4, 8),
      baseSettingsFixture.forTest("Top level suite", "Subsuite", "Test in subsuite")
    )
  }

  override fun assetConfigurationSettings(
    messageTracePrefix: String,
    configuration: RunConfiguration?,
    settingsFixture: KarmaSettingsFixture
  ) {
    val karmaRunConfiguration = configuration as? KarmaRunConfiguration
    assertNotNull("$messageTracePrefix: Configuration has correct type", karmaRunConfiguration)

    val settings = karmaRunConfiguration?.runSettings
    assertNotNull("$messageTracePrefix: Configuration has settings", settings)

    settings?.let { karmaConfigSettings ->
      settingsFixture.workingDir?.let {
        assertEquals(
          "$messageTracePrefix: Working dirs are same",
          makeProjectRelativePathAsAbsolute(it),
          karmaConfigSettings.workingDirectorySystemIndependent
        )
      }
      assertEquals(
        "$messageTracePrefix: Test kind is same",
        settingsFixture.scopeKind,
        karmaConfigSettings.scopeKind,
      )

      assertEquals(
        "$messageTracePrefix: Test file path is same",
        makeProjectRelativePathAsAbsolute(settingsFixture.testFilePath),
        karmaConfigSettings.testFileSystemIndependentPath,
      )

      settingsFixture.configFilePath?.let {
        assertEquals(
          "$messageTracePrefix: Config file path is same",
          makeProjectRelativePathAsAbsolute(it),
          karmaConfigSettings.configPathSystemIndependent
        )
      }

      settingsFixture.testNames?.let {
        assertEquals(
          "$messageTracePrefix: Test names are same",
          it,
          karmaConfigSettings.testNames,
        )
      }
    }
  }

  class KarmaSettingsFixture(
    val scopeKind: KarmaScopeKind,
    val testFilePath: String,
    val configFilePath: String? = null,
    val testNames: List<String>? = null,
    val workingDir: String? = null,
  ) {
    fun forTest(vararg testNames: String): KarmaSettingsFixture =
      KarmaSettingsFixture(
        KarmaScopeKind.TEST,
        testFilePath,
        configFilePath,
        testNames.toList(),
        workingDir,
      )

    fun forSuite(vararg testNames: String): KarmaSettingsFixture =
      KarmaSettingsFixture(
        KarmaScopeKind.SUITE,
        testFilePath,
        configFilePath,
        testNames.toList(),
        workingDir,
      )
  }
}
