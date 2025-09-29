// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.project

import com.intellij.compiler.server.impl.BuildProcessClasspathManager
import com.intellij.openapi.application.ArchivedCompilationContextUtil
import com.intellij.openapi.project.DefaultProjectFactory
import com.intellij.psi.impl.light.LightJavaModule
import com.intellij.testFramework.fixtures.BareTestFixtureTestCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class BuildProcessClasspathTest : BareTestFixtureTestCase() {
  @Test fun testBuildProcessClasspath() {
    val classpath = BuildProcessClasspathManager(testRootDisposable).getBuildProcessClasspath(DefaultProjectFactory.getInstance().defaultProject)
    val libs = classpath.mapTo(HashSet()) { LightJavaModule.moduleName(File(it).name) }

    // libraries have the same names even when running from archived compilation outputs
    assertThat(libs).contains("biz.aQute.bndlib", "biz.aQute.repository", "biz.aQute.resolve", "plexus.utils")

    val module = "intellij.osgi.jps"
    val mapping = ArchivedCompilationContextUtil.archivedCompiledClassesMapping
    if (mapping != null) {
      assertThat(classpath.toSet()).contains(mapping["production/$module"])
    }
    else {
      assertThat(libs).contains(module)
    }
  }
}