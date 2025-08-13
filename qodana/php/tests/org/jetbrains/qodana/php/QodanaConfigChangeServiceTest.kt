package org.jetbrains.qodana.php

import com.intellij.openapi.application.ApplicationInfo
import com.jetbrains.php.config.PhpLanguageLevel
import com.jetbrains.php.config.PhpProjectConfigurationFacade
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.settings.QodanaConfigChangeService

class QodanaConfigChangeServiceTest: QodanaPluginHeavyTestBase() {
  private val configChangeService get() = QodanaConfigChangeService.getInstance(project)

  fun `test default generated qodana yaml not default php version`() = runDispatchingOnUi {
    configurePhpLanguageLevel(PhpLanguageLevel.PHP700)

    @Language("YAML")
    val expected = """
        #-------------------------------------------------------------------------------#
        #               Qodana analysis is configured by qodana.yaml file               #
        #             https://www.jetbrains.com/help/qodana/qodana-yaml.html            #
        #-------------------------------------------------------------------------------#
  
        #################################################################################
        #              WARNING: Do not store sensitive information in this file,        #
        #               as its contents will be included in the Qodana report.          #
        #################################################################################
        version: "1.0"
        
        #Specify inspection profile for code analysis
        profile:
          name: qodana.starter
        
        #Enable inspections
        #include:
        #  - name: <SomeEnabledInspectionId>
        
        #Disable inspections
        #exclude:
        #  - name: <SomeDisabledInspectionId>
        #    paths:
        #      - <path/where/not/run/inspection>
        
        php:
          version: 7.0 #(Applied in CI/CD pipeline)
        
        #Execute shell command before Qodana execution (Applied in CI/CD pipeline)
        #bootstrap: sh ./prepare-qodana.sh
        
        #Install IDE plugins before Qodana execution (Applied in CI/CD pipeline)
        #plugins:
        #  - id: <plugin.id> #(plugin id can be found at https://plugins.jetbrains.com)
        
        # Quality gate. Will fail the CI/CD pipeline if any condition is not met
        # severityThresholds - configures maximum thresholds for different problem severities
        # testCoverageThresholds - configures minimum code coverage on a whole project and newly added code
        # Code Coverage is available in Ultimate and Ultimate Plus plans
        #failureConditions:
        #  severityThresholds:
        #    any: 15
        #    critical: 5
        #  testCoverageThresholds:
        #    fresh: 70
        #    total: 50
        
        #Specify Qodana linter for analysis (Applied in CI/CD pipeline)
        linter: jetbrains/qodana-<linter>:LINTER_PLACEHOLDER
        
      """.trimIndent().updateVersion()
    val generated = configChangeService.createDefaultConfigContent()
    assertThat(generated).isEqualTo(expected)
  }

  private fun configurePhpLanguageLevel(newLanguageLevel: PhpLanguageLevel) {
    PhpProjectConfigurationFacade.getInstance(project).languageLevel = newLanguageLevel
  }


  private fun String.updateVersion(): String {
    val ideMajorVersion = ApplicationInfo.getInstance().majorVersion
    val ideMinorVersion = ApplicationInfo.getInstance().minorVersionMainPart
    return this.replace("LINTER_PLACEHOLDER","${ideMajorVersion}.${ideMinorVersion}")
  }
}