// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.run

import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.use
import org.gradle.util.GradleVersion
import org.jetbrains.plugins.gradle.execution.build.GradleProjectTaskRunnerTestCase
import org.jetbrains.plugins.gradle.testFramework.annotations.BaseGradleVersionSource
import org.junit.jupiter.params.ParameterizedTest

class CucumberGradleProjectTaskRunnerTest : GradleProjectTaskRunnerTestCase() {

  @ParameterizedTest(name = "[{index}] {0} delegatedBuild={1}, delegatedRun={2}")
  @BaseGradleVersionSource("""
    true:true:   true:false,
    true:false:  true:false,
    false:true:  false:false,
    false:false: false:false
  """)
  fun `test GradleProjectTaskRunner#canRun for CucumberJavaRunConfiguration`(
    gradleVersion: GradleVersion,
    delegatedBuild: Boolean, delegatedRun: Boolean,
    shouldBuild: Boolean, shouldRun: Boolean,
  ) {
    testJavaProject(gradleVersion) {
      Disposer.newDisposable().use { testDisposable ->
        val configurationType = CucumberJavaRunConfigurationType.getInstance()
        setupGradleDelegationMode(delegatedBuild, delegatedRun, testDisposable)
        assertGradleProjectTaskRunnerCanRun(configurationType, shouldBuild, shouldRun)
      }
    }
  }
}