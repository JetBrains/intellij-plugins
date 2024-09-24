// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.run

import org.gradle.util.GradleVersion
import org.jetbrains.plugins.gradle.execution.build.GradleProjectTaskRunnerTestCase
import org.jetbrains.plugins.gradle.testFramework.annotations.BaseGradleVersionSource
import org.junit.jupiter.params.ParameterizedTest

class CucumberGradleTaskRunnerTest : GradleProjectTaskRunnerTestCase() {

  @ParameterizedTest
  @BaseGradleVersionSource
  fun `test GradleProjectTaskRunner#canRun with Java cucumber configuration`(gradleVersion: GradleVersion) {
    testEmptyProject(gradleVersion) {
      `test GradleProjectTaskRunner#canRun`(
        CucumberJavaRunConfigurationType.getInstance(),
        shouldRunWithModule = false,
        shouldRunWithoutModule = false,
        shouldBuildWithModule = true,
        shouldBuildWithoutModule = false
      )
    }
  }
}